package com.example.healthmateapp.screens.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.healthmateapp.R

class MedicationAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medication"
        val medicationDosage = intent.getStringExtra(EXTRA_MEDICATION_DOSAGE) ?: ""
        val medicationNote = intent.getStringExtra(EXTRA_MEDICATION_NOTE) ?: ""
        val medicationId = intent.getStringExtra(EXTRA_MEDICATION_ID) ?: ""

        // Create notification channel for Android O and above
        createNotificationChannel(context)

        // Launch full-screen alarm activity
        val alarmIntent = Intent(context, MedicationAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
            putExtra(EXTRA_MEDICATION_DOSAGE, medicationDosage)
            putExtra(EXTRA_MEDICATION_NOTE, medicationNote)
            putExtra(EXTRA_MEDICATION_ID, medicationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            medicationId.hashCode(),
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Show notification
        showNotification(context, medicationName, medicationDosage, medicationNote, pendingIntent)

        // Start alarm activity
        context.startActivity(alarmIntent)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for medication reminders"
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    null
                )
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(
        context: Context,
        medicationName: String,
        medicationDosage: String,
        medicationNote: String,
        pendingIntent: PendingIntent
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_medication) // You'll need to add this icon
            .setContentTitle("Time to take your medication!")
            .setContentText("$medicationName - $medicationDosage")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$medicationName\n$medicationDosage\n$medicationNote")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        notificationManager.notify(medicationName.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "medication_alarm_channel"
        const val EXTRA_MEDICATION_NAME = "medication_name"
        const val EXTRA_MEDICATION_DOSAGE = "medication_dosage"
        const val EXTRA_MEDICATION_NOTE = "medication_note"
        const val EXTRA_MEDICATION_ID = "medication_id"
    }
}