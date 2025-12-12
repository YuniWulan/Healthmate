package com.example.healthmateapp.screens.alarm

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat

/**
 * Helper class to request alarm and notification permissions
 */
class PermissionHelper(private val activity: ComponentActivity) {

    private var onPermissionResult: ((Boolean) -> Unit)? = null

    private val notificationPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult?.invoke(isGranted)
    }

    /**
     * Check and request all necessary permissions for medication alarms
     */
    fun checkAndRequestPermissions(onResult: (Boolean) -> Unit) {
        onPermissionResult = onResult

        // Check notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotificationPermission = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasNotificationPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        // Check exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Need to direct user to settings
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                activity.startActivity(intent)
                return
            }
        }

        // All permissions granted
        onResult(true)
    }

    companion object {
        /**
         * Check if all required permissions are granted
         */
        fun hasAllPermissions(context: Context): Boolean {
            // Check notification permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasNotificationPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasNotificationPermission) return false
            }

            // Check exact alarm permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) return false
            }

            return true
        }
    }
}

/**
 * Composable to show permission request dialog
 */
@Composable
fun PermissionRequestDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Permission Required") },
            text = {
                Text(
                    "This app needs permission to schedule alarms and show notifications " +
                            "to remind you about your medications at the right time."
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Not Now")
                }
            }
        )
    }
}