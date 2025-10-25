package com.aircalc.converter.presentation.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aircalc.converter.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/**
 * Foreground service that manages cooking timer with the following features:
 * - SystemClock-based timing for accuracy
 * - Wake lock to prevent deep sleep interruption
 * - DataStore persistence to survive process death
 * - AlarmManager backup for guaranteed completion
 * - Comprehensive error handling
 * - Persistent notification after completion
 */
class TimerService : Service() {

    companion object {
        private const val TAG = "TimerService"
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1

        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_RESET = "ACTION_RESET"
        const val ACTION_DISMISS = "ACTION_DISMISS"
        const val ACTION_ALARM_TRIGGER = "ACTION_ALARM_TRIGGER"

        const val EXTRA_DURATION_MINUTES = "EXTRA_DURATION_MINUTES"

        // DataStore
        private val Context.timerDataStore by preferencesDataStore(name = "timer_prefs")
        private val KEY_END_TIME = longPreferencesKey("end_time")
        private val KEY_TOTAL_SECONDS = intPreferencesKey("total_seconds")
        private val KEY_IS_RUNNING = booleanPreferencesKey("is_running")
        private val KEY_IS_PAUSED = booleanPreferencesKey("is_paused")

        private val _timerState = MutableStateFlow(TimerServiceState())
        val timerState: StateFlow<TimerServiceState> = _timerState.asStateFlow()

        // Track if app is in foreground
        @Volatile
        private var isAppInForeground = false

        fun setAppInForeground(inForeground: Boolean) {
            isAppInForeground = inForeground
            Log.d(TAG, "App foreground state changed: $inForeground")
        }

        fun isAppInForeground(): Boolean = isAppInForeground

        fun start(context: Context, durationMinutes: Int) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DURATION_MINUTES, durationMinutes)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pause(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            // Clear state immediately before sending intent
            _timerState.value = TimerServiceState()

            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var alarmManager: AlarmManager? = null
    private var endTimeMillis: Long = 0
    private var totalSeconds = 0
    private var pausedRemainingSeconds = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Attempt to restore state on service creation
        serviceScope.launch {
            restoreTimerState()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val minutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 0)
                if (minutes > 0) {
                    startTimer(minutes)
                }
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
            ACTION_RESET -> resetTimer()
            ACTION_DISMISS -> dismissTimer()
            ACTION_ALARM_TRIGGER -> {
                // AlarmManager triggered - timer finished
                Log.d(TAG, "Timer completed via AlarmManager")
                onTimerFinished()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTimer(minutes: Int) {
        // Cancel any existing timer job first
        timerJob?.cancel()

        // Reset to clean state
        totalSeconds = minutes * 60
        endTimeMillis = System.currentTimeMillis() + (totalSeconds * 1000L)
        pausedRemainingSeconds = 0

        _timerState.value = TimerServiceState(
            isRunning = true,
            isFinished = false,
            totalSeconds = totalSeconds,
            remainingSeconds = totalSeconds,
            remainingMinutes = minutes,
            formattedTime = formatTime(totalSeconds)
        )

        // Acquire wake lock
        acquireWakeLock()

        // Schedule AlarmManager as backup
        scheduleAlarm(endTimeMillis)

        // Save state
        serviceScope.launch {
            saveTimerState()
        }

        // Start foreground with notification
        // If app is in foreground, we still need to call startForeground (Android requirement)
        // but we'll use a minimal notification that won't be intrusive
        val notification = if (isAppInForeground) {
            createMinimalNotification()
        } else {
            createNotification()
        }
        startForeground(NOTIFICATION_ID, notification)

        // Start timer job
        startTimerJob()
    }

    private fun startTimerJob() {
        timerJob?.cancel()

        timerJob = serviceScope.launch {
            var lastNotificationUpdate = 0L
            var lastMinute = -1

            // Wait 1 second BEFORE first calculation to preserve initial display
            while (System.currentTimeMillis() < endTimeMillis && _timerState.value.isRunning) {
                delay(1000)

                if (!_timerState.value.isRunning) break

                val remainingMillis = endTimeMillis - System.currentTimeMillis()
                val remainingSeconds = maxOf(0, (remainingMillis / 1000).toInt())
                val mins = remainingSeconds / 60

                _timerState.value = _timerState.value.copy(
                    remainingSeconds = remainingSeconds,
                    remainingMinutes = mins,
                    formattedTime = formatTime(remainingSeconds)
                )

                // Update notification only when minute changes or every 30 seconds in background
                val currentTime = System.currentTimeMillis()
                val shouldUpdateNotification = if (isAppInForeground) {
                    mins != lastMinute // Only when minute changes
                } else {
                    // In background: update when minute changes OR every 30 seconds
                    currentTime - lastNotificationUpdate > 30000 || mins != lastMinute
                }

                if (shouldUpdateNotification) {
                    lastNotificationUpdate = currentTime
                    lastMinute = mins

                    // Update notification based on foreground state
                    try {
                        val notification = if (isAppInForeground) {
                            createMinimalNotification()
                        } else {
                            createNotification()
                        }
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(NOTIFICATION_ID, notification)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating notification", e)
                    }
                }

                if (remainingSeconds <= 0) {
                    onTimerFinished()
                    break
                }
            }
        }
    }

    private fun pauseTimer() {
        if (!_timerState.value.isRunning || _timerState.value.isFinished) {
            return
        }

        timerJob?.cancel()

        // Use current state's remaining seconds instead of recalculating
        // This preserves the initial display value (e.g., 20:00 instead of 19:59)
        pausedRemainingSeconds = _timerState.value.remainingSeconds

        _timerState.value = _timerState.value.copy(isRunning = false)

        // Release wake lock when paused
        releaseWakeLock()

        // Cancel alarm
        cancelAlarm()

        // Update endTimeMillis for potential resume
        endTimeMillis = System.currentTimeMillis() + (pausedRemainingSeconds * 1000L)

        // Save state
        serviceScope.launch {
            saveTimerState()
        }

        // Update notification to show paused state
        try {
            val notification = if (isAppInForeground) {
                createMinimalNotification()
            } else {
                createNotification()
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification on pause", e)
        }
    }

    private fun resumeTimer() {
        if (_timerState.value.isRunning || _timerState.value.isFinished) {
            return
        }

        if (pausedRemainingSeconds <= 0) {
            Log.w(TAG, "Cannot resume timer: no time remaining")
            return
        }

        // Calculate new end time based on paused remaining seconds
        endTimeMillis = System.currentTimeMillis() + (pausedRemainingSeconds * 1000L)

        _timerState.value = _timerState.value.copy(isRunning = true)

        // Acquire wake lock
        acquireWakeLock()

        // Schedule alarm
        scheduleAlarm(endTimeMillis)

        // Save state
        serviceScope.launch {
            saveTimerState()
        }

        startTimerJob()
    }

    private fun stopTimer() {
        timerJob?.cancel()
        cancelAlarm()
        releaseWakeLock()

        // Reset instance variables to ensure clean state for restart
        endTimeMillis = 0
        totalSeconds = 0
        pausedRemainingSeconds = 0

        _timerState.value = TimerServiceState()

        // Clear persisted state
        serviceScope.launch {
            clearTimerState()
        }

        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping foreground", e)
        }

        // Stop the service immediately - MainActivity will restart if needed
        stopSelf()
    }

    private fun resetTimer() {
        // Reset the timer to the original duration and start it paused
        if (totalSeconds > 0) {
            val minutes = totalSeconds / 60
            Log.d(TAG, "Resetting timer to $minutes minutes")

            // Stop current timer
            timerJob?.cancel()
            cancelAlarm()

            // Restart with original duration
            startTimer(minutes)

            // Immediately pause it so user can start when ready
            pauseTimer()
        } else {
            Log.w(TAG, "Cannot reset timer: no original duration stored")
        }
    }

    private fun dismissTimer() {
        // Dismiss the finished notification
        stopTimer()
    }

    private fun onTimerFinished() {
        Log.d(TAG, "Timer finished - starting completion sequence")

        timerJob?.cancel()
        cancelAlarm()
        releaseWakeLock()

        _timerState.value = _timerState.value.copy(
            isFinished = true,
            isRunning = false,
            remainingSeconds = 0,
            formattedTime = "00:00"
        )

        // Clear persisted state
        serviceScope.launch {
            clearTimerState()
        }

        // Create finished notification first
        val finishedNotification = createNotification(isFinished = true)

        // IMPORTANT: Update to foreground with finished notification before other operations
        try {
            startForeground(NOTIFICATION_ID, finishedNotification)
            Log.d(TAG, "Finished notification displayed in foreground")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing finished notification", e)
        }

        // Vibrate
        try {
            vibrate()
            Log.d(TAG, "Vibration triggered")
        } catch (e: SecurityException) {
            Log.e(TAG, "Vibration permission denied", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error during vibration", e)
        }

        // Play alarm sound
        serviceScope.launch(Dispatchers.Main) {
            try {
                Log.d(TAG, "Starting alarm sound")
                playAlarmSound()
                Log.d(TAG, "Alarm sound completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error playing alarm sound", e)
            }
        }

        // Keep service running with finished notification (P2 fix)
        // User must dismiss or stop manually
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
        }
    }

    /**
     * Play alarm sound with proper resource management and cancellation support.
     * Uses withContext to ensure cleanup even on coroutine cancellation.
     */
    private suspend fun playAlarmSound() = withContext(Dispatchers.Main) {
        var mediaPlayer: MediaPlayer? = null
        try {
            ensureActive() // Check if coroutine is still active

            // Try default alarm sound first
            var soundUri = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
            Log.d(TAG, "Attempting to play alarm from URI: $soundUri")

            mediaPlayer = MediaPlayer()

            try {
                mediaPlayer.setDataSource(applicationContext, soundUri)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set default alarm URI, trying notification URI", e)
                // Fallback to notification sound
                soundUri = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
                mediaPlayer.reset()
                try {
                    mediaPlayer.setDataSource(applicationContext, soundUri)
                } catch (e2: Exception) {
                    Log.w(TAG, "Failed to set notification URI, trying ringtone URI", e2)
                    // Fallback to ringtone
                    soundUri = android.provider.Settings.System.DEFAULT_RINGTONE_URI
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(applicationContext, soundUri)
                }
            }

            ensureActive() // Check before setting audio attributes

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            } else {
                @Suppress("DEPRECATION")
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM)
            }

            mediaPlayer.setVolume(1.0f, 1.0f)
            mediaPlayer.isLooping = false

            Log.d(TAG, "Preparing media player...")
            mediaPlayer.prepare()

            ensureActive() // Check before starting
            Log.d(TAG, "Starting playback...")
            mediaPlayer.start()

            Log.d(TAG, "Media player started, waiting 5 seconds...")

            // Play for 5 seconds with cancellation checks
            repeat(5) {
                delay(1000)
                ensureActive() // Check if coroutine cancelled during playback
            }

            if (mediaPlayer.isPlaying) {
                Log.d(TAG, "Stopping media player...")
                mediaPlayer.stop()
            } else {
                Log.w(TAG, "Media player was not playing after 5 seconds")
            }
        } catch (e: CancellationException) {
            Log.d(TAG, "Alarm sound playback cancelled")
            throw e // Re-throw to propagate cancellation
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound: ${e.message}", e)
        } finally {
            // Cleanup always happens, even on cancellation
            try {
                mediaPlayer?.release()
                Log.d(TAG, "Media player released")
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing media player", e)
            }
        }
    }

    private fun createMinimalNotification(): Notification {
        // Low-priority notification for when app is in foreground
        // Shows timer but won't pop up as heads-up notification
        val state = _timerState.value

        val contentIntent = packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val text = if (state.isRunning) {
            "Time remaining: ${formatMinutesOnly(state.remainingSeconds)}"
        } else {
            "Paused: ${formatMinutesOnly(state.remainingSeconds)}"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Cooking Timer")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN) // Minimum priority - no heads-up
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .build()
    }

    private fun createNotification(isFinished: Boolean = false): Notification {
        val state = _timerState.value

        val contentIntent = packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val title = if (isFinished) {
            "Timer Finished!"
        } else {
            "Cooking Timer"
        }

        val text = if (isFinished) {
            "Your food is ready!"
        } else if (state.isRunning) {
            "Time remaining: ${formatMinutesOnly(state.remainingSeconds)}"
        } else {
            "Paused: ${formatMinutesOnly(state.remainingSeconds)}"
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentIntent)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Different priority for countdown vs completion
        if (isFinished) {
            // Completion: Higher priority but no heads-up/full-screen
            builder
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOnlyAlertOnce(false) // Alert for completion
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("Your food is ready! Tap to open the app.")
                    .setBigContentTitle("Timer Finished!"))
        } else {
            // Countdown: Low priority, silent, no interruption, no popup
            builder
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN) // MIN to prevent any popup
                .setOnlyAlertOnce(true) // Silent updates
                .setSilent(true) // Explicitly silent
        }

        // Add action buttons
        if (isFinished) {
            // Dismiss button for finished notification
            val dismissIntent = Intent(this, TimerService::class.java).apply {
                action = ACTION_DISMISS
            }
            val dismissPendingIntent = PendingIntent.getService(
                this,
                4,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(R.drawable.ic_stop, "Dismiss", dismissPendingIntent)
        } else {
            if (state.isRunning) {
                val pauseIntent = Intent(this, TimerService::class.java).apply {
                    action = ACTION_PAUSE
                }
                val pausePendingIntent = PendingIntent.getService(
                    this,
                    1,
                    pauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(R.drawable.ic_pause, "Pause", pausePendingIntent)
            } else {
                val resumeIntent = Intent(this, TimerService::class.java).apply {
                    action = ACTION_RESUME
                }
                val resumePendingIntent = PendingIntent.getService(
                    this,
                    2,
                    resumeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(R.drawable.ic_play, "Resume", resumePendingIntent)
            }

            val resetIntent = Intent(this, TimerService::class.java).apply {
                action = ACTION_RESET
            }
            val resetPendingIntent = PendingIntent.getService(
                this,
                3,
                resetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(R.drawable.ic_stop, "Reset", resetPendingIntent)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Cooking Timer",
                NotificationManager.IMPORTANCE_LOW  // Low importance - allows buttons, no popup
            ).apply {
                description = "Notifications for cooking timer"
                setSound(null, null) // We'll handle sound ourselves
                enableVibration(false) // We'll handle vibration ourselves
                setShowBadge(false) // Don't show badge
                setBypassDnd(false) // Don't bypass Do Not Disturb
                enableLights(false) // No LED lights
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(mins, secs)
    }

    /**
     * Format time for notifications showing only minutes.
     * Used since notifications update less frequently (only when minutes change).
     */
    private fun formatMinutesOnly(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60

        return when {
            mins > 0 && secs > 0 -> "$mins min"
            mins > 0 -> "$mins min"
            secs > 0 -> "< 1 min"
            else -> "0 min"
        }
    }

    /**
     * Acquire wake lock with timeout based on timer duration.
     * Prevents deep sleep from interrupting the timer countdown.
     * Timeout is calculated as remaining time plus 5 minute buffer for safety.
     */
    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "AirCalc::TimerWakeLock"
                )
            }

            // Calculate timeout based on remaining time plus 5 minute buffer
            val remainingMs = (endTimeMillis - System.currentTimeMillis()).coerceAtLeast(0)
            val bufferMs = 5 * 60 * 1000L // 5 minute buffer
            val timeoutMs = remainingMs + bufferMs

            wakeLock?.acquire(timeoutMs)
            Log.d(TAG, "Wake lock acquired for ${timeoutMs / 1000} seconds")
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring wake lock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "Wake lock released")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing wake lock", e)
        }
    }

    // P1: AlarmManager backup
    private fun scheduleAlarm(triggerTimeMillis: Long) {
        try {
            val alarmIntent = Intent(this, TimerAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager?.let { am ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    am.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
                Log.d(TAG, "Alarm scheduled for ${java.util.Date(triggerTimeMillis)}")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for exact alarm", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm", e)
        }
    }

    private fun cancelAlarm() {
        try {
            val alarmIntent = Intent(this, TimerAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager?.cancel(pendingIntent)
            Log.d(TAG, "Alarm cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling alarm", e)
        }
    }

    // P0: State persistence
    private suspend fun saveTimerState() {
        try {
            timerDataStore.edit { prefs ->
                prefs[KEY_END_TIME] = endTimeMillis
                prefs[KEY_TOTAL_SECONDS] = totalSeconds
                prefs[KEY_IS_RUNNING] = _timerState.value.isRunning
                prefs[KEY_IS_PAUSED] = !_timerState.value.isRunning && pausedRemainingSeconds > 0
            }
            Log.d(TAG, "Timer state saved")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving timer state", e)
        }
    }

    private suspend fun restoreTimerState() {
        try {
            val prefs = timerDataStore.data.first()
            val savedEndTime = prefs[KEY_END_TIME] ?: 0L
            val savedTotalSeconds = prefs[KEY_TOTAL_SECONDS] ?: 0
            val wasRunning = prefs[KEY_IS_RUNNING] ?: false
            val wasPaused = prefs[KEY_IS_PAUSED] ?: false

            if (savedEndTime > 0 && savedTotalSeconds > 0) {
                val now = System.currentTimeMillis()

                if (wasRunning && savedEndTime > now) {
                    // Timer was running, restore it
                    endTimeMillis = savedEndTime
                    totalSeconds = savedTotalSeconds

                    _timerState.value = TimerServiceState(
                        isRunning = true,
                        isFinished = false,
                        totalSeconds = totalSeconds,
                        remainingSeconds = ((savedEndTime - now) / 1000).toInt(),
                        formattedTime = formatTime(((savedEndTime - now) / 1000).toInt())
                    )

                    acquireWakeLock()
                    scheduleAlarm(endTimeMillis)
                    startForeground(NOTIFICATION_ID, createNotification())
                    startTimerJob()

                    Log.d(TAG, "Timer state restored and running")
                } else if (wasPaused) {
                    // Timer was paused, calculate remaining time
                    val remainingMillis = savedEndTime - now
                    pausedRemainingSeconds = maxOf(0, (remainingMillis / 1000).toInt())
                    totalSeconds = savedTotalSeconds

                    if (pausedRemainingSeconds > 0) {
                        _timerState.value = TimerServiceState(
                            isRunning = false,
                            isFinished = false,
                            totalSeconds = totalSeconds,
                            remainingSeconds = pausedRemainingSeconds,
                            formattedTime = formatTime(pausedRemainingSeconds)
                        )

                        startForeground(NOTIFICATION_ID, createNotification())
                        Log.d(TAG, "Timer state restored in paused state")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring timer state", e)
        }
    }

    private suspend fun clearTimerState() {
        try {
            timerDataStore.edit { it.clear() }
            Log.d(TAG, "Timer state cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing timer state", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        releaseWakeLock()
        serviceScope.cancel()
    }
}

/**
 * State of the timer service.
 */
data class TimerServiceState(
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val totalSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val remainingMinutes: Int = 0,
    val formattedTime: String = "00:00"
)
