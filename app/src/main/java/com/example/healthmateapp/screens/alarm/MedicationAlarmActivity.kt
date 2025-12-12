package com.example.healthmateapp.screens.alarm

import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmateapp.ui.theme.BlueMain

class MedicationAlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show the alarm even when screen is locked
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val medicationName = intent.getStringExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NAME) ?: "Medication"
        val medicationDosage = intent.getStringExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_DOSAGE) ?: ""
        val medicationNote = intent.getStringExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NOTE) ?: ""
        val medicationId = intent.getStringExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID) ?: ""

        // Play alarm sound
        playAlarmSound()

        setContent {
            MaterialTheme {
                MedicationAlarmScreen(
                    medicationName = medicationName,
                    medicationDosage = medicationDosage,
                    medicationNote = medicationNote,
                    onDismiss = {
                        stopAlarmSound()
                        finish()
                    },
                    onSnooze = {
                        stopAlarmSound()
                        // Schedule snooze alarm (5 minutes later)
                        MedicationAlarmScheduler.scheduleSnooze(
                            context = this,
                            medicationId = medicationId,
                            medicationName = medicationName,
                            medicationDosage = medicationDosage,
                            medicationNote = medicationNote
                        )
                        finish()
                    }
                )
            }
        }
    }

    private fun playAlarmSound() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@MedicationAlarmActivity, alarmUri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAlarmSound() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmSound()
    }
}

@Composable
fun MedicationAlarmScreen(
    medicationName: String,
    medicationDosage: String,
    medicationNote: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueMain),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = "Medication",
                modifier = Modifier.size(120.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Time to Take Your Medication!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Medication Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = medicationName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueMain,
                        textAlign = TextAlign.Center
                    )

                    if (medicationDosage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = medicationDosage,
                            fontSize = 18.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (medicationNote.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = medicationNote,
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Dismiss Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = BlueMain
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "I've Taken It",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Snooze Button
            OutlinedButton(
                onClick = onSnooze,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 2.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(Color.White)
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    text = "Snooze (5 min)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}