package com.aircalc.converter.presentation.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aircalc.converter.MainActivity
import com.aircalc.converter.R
import com.aircalc.converter.util.Constants
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

/**
 * BroadcastReceiver that handles AlarmManager triggers when cooking timer completes.
 *
 * This receiver is the completion handler for timers scheduled by [TimerManager].
 * It provides multi-sensory notification through:
 * - **Visual**: High-priority heads-up notification
 * - **Audio**: Default alarm sound (respects Do Not Disturb)
 * - **Haptic**: Vibration pattern
 *
 * ## Android Integration:
 * - Registered in AndroidManifest.xml
 * - Triggered by AlarmManager.setExactAndAllowWhileIdle()
 * - Handles background work with BroadcastReceiver.goAsync()
 * - Creates notification channel on first run (Android O+)
 *
 * ## Notification Behavior:
 * - Uses IMPORTANCE_HIGH for sound and heads-up display
 * - Tapping notification opens app via MainActivity
 * - Auto-dismisses when tapped
 * - Plays alarm sound for [Constants.Timer.ALARM_SOUND_DURATION_MS]
 *
 * ## Permissions Required:
 * - POST_NOTIFICATIONS (Android 13+)
 * - VIBRATE
 *
 * @see TimerManager for timer scheduling
 * @see Constants.Timer for configuration
 * @see Constants.Vibration for vibration patterns
 */
class TimerAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "TimerAlarmReceiver"
        /** Notification channel ID for timer completion notifications */
        private const val CHANNEL_ID = "timer_completion_channel"
        /** Unique notification ID to allow updates/cancellation */
        private const val NOTIFICATION_ID = 2001
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Timer alarm received - food is ready!")

        // Create notification channel if needed
        createNotificationChannel(context)

        // Show notification
        showCompletionNotification(context)

        // Play alarm sound and vibrate in background
        playAlarmAndVibrate(context)
    }

    /**
     * Create notification channel for timer completion.
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.timer_notification_channel_name)
            val descriptionText = context.getString(R.string.timer_notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH // High for sound and heads-up
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = Constants.Vibration.PATTERN
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Show notification that timer has completed.
     */
    private fun showCompletionNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to open app when notification is tapped
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.timer_complete_title))
            .setContentText(context.getString(R.string.timer_complete_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .setVibrate(Constants.Vibration.PATTERN)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        Log.d(TAG, "Completion notification shown")
    }

    /**
     * Play alarm sound and vibrate.
     * Uses a local coroutine scope for the receiver lifecycle.
     */
    private fun playAlarmAndVibrate(context: Context) {
        // Use goAsync() for background work in BroadcastReceiver
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Play alarm sound
                playAlarmSound(context)

                // Vibrate
                vibrateDevice(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error playing alarm", e)
            } finally {
                // Finish the broadcast
                pendingResult.finish()
            }
        }
    }

    /**
     * Play alarm sound for configured duration.
     */
    private suspend fun playAlarmSound(context: Context) {
        var mediaPlayer: MediaPlayer? = null
        try {
            coroutineContext.ensureActive()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )

                // Use default alarm sound
                setDataSource(
                    context,
                    android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
                        ?: android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
                )

                prepare()
                start()
            }

            Log.d(TAG, "Alarm sound started")

            // Play for configured duration
            val durationMs = Constants.Timer.ALARM_SOUND_DURATION_MS
            val checkIntervalMs = 500L
            var elapsed = 0L

            while (elapsed < durationMs && mediaPlayer.isPlaying) {
                delay(checkIntervalMs)
                coroutineContext.ensureActive()
                elapsed += checkIntervalMs
            }

        } catch (e: CancellationException) {
            Log.d(TAG, "Alarm sound playback cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound", e)
        } finally {
            try {
                mediaPlayer?.release()
                Log.d(TAG, "MediaPlayer released")
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaPlayer", e)
            }
        }
    }

    /**
     * Vibrate the device with configured pattern.
     */
    private fun vibrateDevice(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(
                    Constants.Vibration.PATTERN,
                    Constants.Vibration.AMPLITUDES,
                    -1 // Don't repeat
                )
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(Constants.Vibration.PATTERN, -1)
            }

            Log.d(TAG, "Vibration triggered")
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating device", e)
        }
    }
}
