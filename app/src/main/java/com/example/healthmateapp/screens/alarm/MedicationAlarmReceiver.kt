package com.example.healthmateapp.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.healthmateapp.screens.alarm.MedicationAlarmActivity

class MedicationAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d(TAG, "🔔 ========== ALARM TRIGGERED ==========")
        android.util.Log.d(TAG, "Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(System.currentTimeMillis())}")

        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medication"
        val medicationDosage = intent.getStringExtra(EXTRA_MEDICATION_DOSAGE) ?: ""
        val medicationNote = intent.getStringExtra(EXTRA_MEDICATION_NOTE) ?: ""
        val medicationId = intent.getStringExtra(EXTRA_MEDICATION_ID) ?: ""

        android.util.Log.d(TAG, "📋 Medication Details:")
        android.util.Log.d(TAG, "  - Name: $medicationName")
        android.util.Log.d(TAG, "  - Dosage: $medicationDosage")
        android.util.Log.d(TAG, "  - Note: $medicationNote")
        android.util.Log.d(TAG, "  - ID: $medicationId")

        // Step 1: Create notification channel
        createNotificationChannel(context)

        // Step 2: Create intent for full-screen alarm activity
        val alarmIntent = Intent(context, MedicationAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
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

        // Step 3: Show notification
        showNotification(
            context,
            medicationName,
            medicationDosage,
            medicationNote,
            medicationId,
            pendingIntent
        )

        // Step 4: Start alarm activity
        try {
            context.startActivity(alarmIntent)
            android.util.Log.d(TAG, "✅ Alarm activity started successfully")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Failed to start alarm activity", e)
            android.util.Log.e(TAG, "Error details: ${e.message}")
            android.util.Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
        }

        android.util.Log.d(TAG, "========================================")
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Medication Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for medication reminders"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500)
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                        null
                    )
                    setShowBadge(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)

                android.util.Log.d(TAG, "✅ Notification channel created successfully")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "❌ Failed to create notification channel", e)
            }
        }
    }

    private fun showNotification(
        context: Context,
        medicationName: String,
        medicationDosage: String,
        medicationNote: String,
        medicationId: String,
        pendingIntent: PendingIntent
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Build notification content
            val contentText = buildString {
                append(medicationName)
                if (medicationDosage.isNotEmpty()) {
                    append(" - $medicationDosage")
                }
            }

            val bigText = buildString {
                append("💊 $medicationName\n")
                if (medicationDosage.isNotEmpty()) {
                    append("📋 Dosage: $medicationDosage\n")
                }
                if (medicationNote.isNotEmpty()) {
                    append("📝 Note: $medicationNote")
                }
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // System icon that always exists
                .setContentTitle("⏰ Time to take your medication!")
                .setContentText(contentText)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(bigText)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true) // This shows the full-screen activity
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Show on lock screen
                .setOngoing(false)
                .build()

            val notificationId = medicationId.hashCode()
            notificationManager.notify(notificationId, notification)

            android.util.Log.d(TAG, "✅ Notification shown successfully (ID: $notificationId)")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Failed to show notification", e)
            android.util.Log.e(TAG, "Error details: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "MedicationAlarmReceiver"
        const val CHANNEL_ID = "medication_alarm_channel"
        const val EXTRA_MEDICATION_NAME = "medication_name"
        const val EXTRA_MEDICATION_DOSAGE = "medication_dosage"
        const val EXTRA_MEDICATION_NOTE = "medication_note"
        const val EXTRA_MEDICATION_ID = "medication_id"
    }
}