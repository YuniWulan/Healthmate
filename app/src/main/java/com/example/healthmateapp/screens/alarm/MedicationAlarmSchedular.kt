package com.example.healthmateapp.screens.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

object MedicationAlarmScheduler {

    fun scheduleAlarm(
        context: Context,
        medicationId: String,
        medicationName: String,
        medicationDosage: String,
        medicationNote: String,
        date: LocalDate,
        time: LocalTime
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Create intent for the alarm receiver
        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_DOSAGE, medicationDosage)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NOTE, medicationNote)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            generateRequestCode(medicationId, date, time),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Convert LocalDate and LocalTime to Calendar
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, date.year)
            set(Calendar.MONTH, date.monthValue - 1) // Calendar months are 0-based
            set(Calendar.DAY_OF_MONTH, date.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val triggerTime = calendar.timeInMillis

        // Only schedule if the time is in the future
        if (triggerTime > System.currentTimeMillis()) {
            // Schedule the alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For Android 6.0 and above, use setExactAndAllowWhileIdle
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                // For older versions
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }
    }

    /**
     * Schedule alarms for a medication across multiple dates
     */
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
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            // Only schedule if the date is today or in the future
            val now = LocalDate.now()
            if (!currentDate.isBefore(now)) {
                scheduleAlarm(
                    context = context,
                    medicationId = medicationId,
                    medicationName = medicationName,
                    medicationDosage = medicationDosage,
                    medicationNote = medicationNote,
                    date = currentDate,
                    time = time
                )
            }
            currentDate = currentDate.plusDays(1)
        }
    }

    /**
     * Cancel a scheduled alarm
     */
    fun cancelAlarm(
        context: Context,
        medicationId: String,
        date: LocalDate,
        time: LocalTime
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, MedicationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            generateRequestCode(medicationId, date, time),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    /**
     * Cancel all alarms for a medication across a date range
     */
    fun cancelAlarmsForDateRange(
        context: Context,
        medicationId: String,
        startDate: LocalDate,
        endDate: LocalDate,
        time: LocalTime
    ) {
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            cancelAlarm(context, medicationId, currentDate, time)
            currentDate = currentDate.plusDays(1)
        }
    }

    /**
     * Schedule a snooze alarm (5 minutes from now)
     */
    fun scheduleSnooze(
        context: Context,
        medicationId: String,
        medicationName: String,
        medicationDosage: String,
        medicationNote: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, "${medicationId}_snooze")
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_DOSAGE, medicationDosage)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NOTE, medicationNote)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "${medicationId}_snooze".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule for 5 minutes from now
        val triggerTime = System.currentTimeMillis() + (5 * 60 * 1000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    /**
     * Generate a unique request code for each alarm based on medication ID, date, and time
     */
    private fun generateRequestCode(
        medicationId: String,
        date: LocalDate,
        time: LocalTime
    ): Int {
        return "${medicationId}_${date}_${time}".hashCode()
    }
}