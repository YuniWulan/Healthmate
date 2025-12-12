package com.example.healthmateapp.screens.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.healthmateapp.alarm.MedicationAlarmReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object MedicationAlarmScheduler {

    // ------------------------------------------------------------
    // 🔔 MAIN SCHEDULING FOR MEDICATION (Needed by ViewModel)
    // ------------------------------------------------------------

    fun scheduleAlarmsForDateRange(
        context: Context,
        medicationId: String,
        medicationName: String,
        medicationDosage: String,
        medicationNote: String,
        startDate: LocalDate,
        endDate: LocalDate,
        time: LocalTime
    ) {
        var current = startDate
        while (!current.isAfter(endDate)) {
            scheduleSingleAlarm(
                context,
                medicationId,
                medicationName,
                medicationDosage,
                medicationNote,
                current,
                time
            )
            current = current.plusDays(1)
        }
    }

    private fun scheduleSingleAlarm(
        context: Context,
        medicationId: String,
        medicationName: String,
        medicationDosage: String,
        medicationNote: String,
        date: LocalDate,
        time: LocalTime
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val triggerDateTime = LocalDateTime.of(date, time)
        val triggerMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_DOSAGE, medicationDosage)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NOTE, medicationNote)
        }

        val requestCode = (medicationId + date.toString()).hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }

        android.util.Log.d("MedicationAlarmScheduler", "⏰ Scheduled alarm for $date at $time")
    }

    fun cancelAlarm(
        context: Context,
        medicationId: String,
        date: LocalDate,
        time: LocalTime
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, MedicationAlarmReceiver::class.java)

        val requestCode = (medicationId + date.toString()).hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        android.util.Log.d(
            "MedicationAlarmScheduler",
            "❌ Alarm cancelled for $medicationId on $date"
        )
    }


    // ------------------------------------------------------------
    // 🔁 SNOOZE SYSTEM (your existing code — unchanged)
    // ------------------------------------------------------------

    fun scheduleSnooze(
        context: Context,
        medicationId: String,
        medicationName: String,
        medicationDosage: String,
        medicationNote: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val snoozeTimeMillis = System.currentTimeMillis() + (5 * 60 * 1000) // 5 minutes

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_DOSAGE, medicationDosage)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NOTE, medicationNote)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, "${medicationId}_snooze")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "${medicationId}_snooze".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        snoozeTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        snoozeTimeMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTimeMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("MedicationAlarmScheduler", "❌ Failed to schedule snooze alarm", e)
        }
    }

    fun cancelSnooze(context: Context, medicationId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicationAlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "${medicationId}_snooze".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        android.util.Log.d("MedicationAlarmScheduler", "❌ Snooze alarm cancelled")
    }
}

