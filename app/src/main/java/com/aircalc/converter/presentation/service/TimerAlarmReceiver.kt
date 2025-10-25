package com.aircalc.converter.presentation.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * BroadcastReceiver that handles AlarmManager triggers for the cooking timer.
 * This ensures the timer completes even if the app is killed or in doze mode.
 */
class TimerAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "TimerAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received - triggering timer completion")

        // Send intent to TimerService to handle completion
        val serviceIntent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_ALARM_TRIGGER
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting TimerService from alarm", e)
        }
    }
}
