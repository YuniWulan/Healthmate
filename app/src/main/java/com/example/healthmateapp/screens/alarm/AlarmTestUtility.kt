package com.example.healthmateapp.screens.alarm

import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*


object AlarmTestUtility {

    private const val TAG = "AlarmTestUtility"

    /**
     * Run comprehensive alarm system check
     */
    fun runDiagnostics(context: Context): String {
        val report = StringBuilder()
        report.append("=== ALARM SYSTEM DIAGNOSTICS ===\n\n")

        // 1. Check Android Version
        report.append("📱 Android Version: ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})\n")

        // 2. Check Notification Permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotificationPerm = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            report.append("🔔 Notification Permission: ${if (hasNotificationPerm) "✅ GRANTED" else "❌ DENIED"}\n")
        } else {
            report.append("🔔 Notification Permission: ✅ Not required (Android < 13)\n")
        }

        // 3. Check Exact Alarm Permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val canScheduleExact = alarmManager.canScheduleExactAlarms()

            report.append("⏰ Exact Alarm Permission: ${if (canScheduleExact) "✅ GRANTED" else "❌ DENIED"}\n")

            if (!canScheduleExact) {
                report.append("   ⚠️ Fix: Settings → Apps → HealthMate → Set alarms and reminders → Allow\n")
            }
        } else {
            report.append("⏰ Exact Alarm Permission: ✅ Not required (Android < 12)\n")
        }

        // 4. Check current time
        val currentTime = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        report.append("\n⏱️ Current Time: ${timeFormat.format(currentTime)}\n")
        report.append("   (Use 24-hour format when setting alarms)\n")

        // 5. Sample alarm times
        report.append("\n📋 Sample Alarm Times:\n")
        val calendar = Calendar.getInstance()

        // 1 minute from now
        calendar.add(Calendar.MINUTE, 1)
        report.append("   • +1 min: ${String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))}\n")

        // 2 minutes from now
        calendar.add(Calendar.MINUTE, 1)
        report.append("   • +2 min: ${String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))}\n")

        // 5 minutes from now
        calendar.add(Calendar.MINUTE, 3)
        report.append("   • +5 min: ${String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))}\n")

        // 6. Final status
        report.append("\n📊 Overall Status: ")

        val notificationOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        val alarmOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else true

        if (notificationOk && alarmOk) {
            report.append("✅ ALL CHECKS PASSED - Ready to schedule alarms!\n")
        } else {
            report.append("❌ MISSING PERMISSIONS - Alarms won't work\n")
            report.append("\n🔧 Required Actions:\n")
            if (!notificationOk) {
                report.append("   1. Grant notification permission\n")
            }
            if (!alarmOk) {
                report.append("   2. Grant exact alarm permission in Settings\n")
            }
        }

        report.append("\n=====================================\n")

        val reportString = report.toString()
        Log.d(TAG, reportString)
        return reportString
    }

    /**
     * Calculate time difference for alarm scheduling
     */
    fun getTimeUntilAlarm(alarmHour: Int, alarmMinute: Int): String {
        val now = Calendar.getInstance()
        val alarm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarmHour)
            set(Calendar.MINUTE, alarmMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If alarm time has passed today, set for tomorrow
            if (timeInMillis <= now.timeInMillis) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val diffMs = alarm.timeInMillis - now.timeInMillis
        val diffMinutes = diffMs / 1000 / 60
        val diffHours = diffMinutes / 60
        val remainingMinutes = diffMinutes % 60

        return if (diffHours > 0) {
            "$diffHours hours $remainingMinutes minutes"
        } else {
            "$diffMinutes minutes"
        }
    }

    /**
     * Validate if an alarm time is valid (in the future)
     */
    fun isAlarmTimeValid(alarmHour: Int, alarmMinute: Int): Boolean {
        val now = Calendar.getInstance()
        val alarm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarmHour)
            set(Calendar.MINUTE, alarmMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return alarm.timeInMillis > now.timeInMillis
    }
}