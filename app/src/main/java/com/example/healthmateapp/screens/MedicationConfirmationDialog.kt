package com.example.healthmateapp.screens.medication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Confirmation dialog that collects taken details: day, time, date, notes, optional photo.
 * onConfirm -> (day, time, date, notes, photoUri?)
 */
@Composable
fun MedicationConfirmationDialog(
    medicationName: String,
    medicationDosage: String,
    medicationNote: String,
    onDismiss: () -> Unit,
    onConfirm: (day: String, time: String, date: String, notes: String, photoUri: Uri?) -> Unit
) {
    val context = LocalContext.current

    // default values
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)

    var selectedDate by remember { mutableStateOf(today) }
    var selectedTime by remember { mutableStateOf("08:00 AM") }
    var notes by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // image picker (simple, uses ACTION_OPEN_DOCUMENT)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            photoUri = uri
        }
    }

    // Date picker helper
    fun showDatePicker(ctx: Context, initDate: LocalDate, onPicked: (LocalDate) -> Unit) {
        val dpd = DatePickerDialog(
            ctx,
            { _, year, month, dayOfMonth ->
                onPicked(LocalDate.of(year, month + 1, dayOfMonth))
            },
            initDate.year,
            initDate.monthValue - 1,
            initDate.dayOfMonth
        )
        dpd.show()
    }

    // Time picker helper (12-hour format)
    fun showTimePicker(ctx: Context, initial: String, onPicked: (String) -> Unit) {
        // parse initial "hh:mm a"
        val cal = Calendar.getInstance()
        try {
            val sdf = java.text.SimpleDateFormat("hh:mm a", Locale.US)
            cal.time = sdf.parse(initial)!!
        } catch (e: Exception) { /* ignore */ }

        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        TimePickerDialog(ctx, { _, h, m ->
            val chosenCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
            }
            val formatted = java.text.SimpleDateFormat("hh:mm a", Locale.US).format(chosenCal.time)
            onPicked(formatted)
        }, hour, minute, false).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "Confirm Medication")
                Text(text = medicationName, modifier = Modifier.padding(top = 4.dp))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Dosage / note summary
                Text(text = "Dosage: $medicationDosage", modifier = Modifier.padding(vertical = 6.dp))
                if (medicationNote.isNotEmpty()) Text(text = "Note: $medicationNote", modifier = Modifier.padding(bottom = 8.dp))

                // Date selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker(context, selectedDate) { selectedDate = it } }
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "Date: ${selectedDate.format(dateFormatter)}")
                    Spacer(Modifier.weight(1f))
                    Text(text = "Change")
                }

                // Time selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker(context, selectedTime) { selectedTime = it } }
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "Time: $selectedTime")
                    Spacer(Modifier.weight(1f))
                    Text(text = "Change")
                }

                // Notes input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // Photo picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { launcher.launch("image/*") }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Attach Photo (optional)")
                    }

                    Spacer(Modifier.width(12.dp))

                    // show small preview if photo selected (coily load)
                    if (photoUri != null) {
                        // try to load using Coil synchronously (small preview). If Coil not available use placeholder.
                        LaunchedEffect(photoUri) {
                            // noop - preview below will update when photoUri changes
                        }
                        // simple text indicator — keep light to avoid heavy image loading in dialog
                        Text(text = "Photo attached", modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val dayName = selectedDate.dayOfWeek.name.take(3) // e.g., MON
                onConfirm(
                    dayName,
                    selectedTime,
                    selectedDate.format(dateFormatter),
                    notes,
                    photoUri
                )
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(12.dp)
    )
}
