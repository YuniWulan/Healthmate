package com.example.healthmateapp.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmateapp.ui.theme.BlueMain
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    onBackClick: () -> Unit,
    onSaveClick: (
        name: String,
        dosage: String,
        frequency: String,
        startDate: LocalDate,
        endDate: LocalDate,
        beforeMeal: Boolean,
        time: String,
        customInstruction: String
    ) -> Unit
) {
    // --- Form state ---
    var medicationName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Once a day") } // default
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(7)) }
    var beforeMeal by remember { mutableStateOf(true) }

    // Times: dynamic list dependent on frequency
    val times = remember { mutableStateListOf<String>() }

    // Dialog state
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showTimeOptionsDialogForIndex by remember { mutableStateOf<Int?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Time options (12-hour)
    val timeOptions = listOf(
        "06:00 AM", "07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM",
        "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM", "05:10 PM",
        "05:56 PM", "05:58 PM", "06:00 PM", "07:00 PM", "07:02 PM", "07:04 PM", "07:06 PM",
        "07:08 PM", "07:10 PM",  "08:00 PM", "09:00 PM", "10:00 PM"
    )

    // Frequency → default times mapping
    fun applyFrequencyDefaults(freq: String) {
        times.clear()
        when (freq) {
            "Once a day" -> times.addAll(listOf("08:00 AM"))
            "Twice a day" -> times.addAll(listOf("08:00 AM", "08:00 PM"))
            "3x Per Week" -> times.addAll(listOf("08:00 AM", "01:00 PM", "08:00 PM"))
            "Every day" -> times.addAll(listOf("08:00 AM"))
            "Every other day" -> times.addAll(listOf("08:00 AM"))
            else -> times.addAll(listOf("08:00 AM"))
        }
    }

    // Initialize times with default frequency once on composition
    LaunchedEffect(Unit) { applyFrequencyDefaults(frequency) }

    // When frequency changes, update times to defaults
    LaunchedEffect(frequency) { applyFrequencyDefaults(frequency) }

    // Date formatter (English month short/day)
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Medication",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xffffff))
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Medication Name
            Text(
                "Medication Name",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = medicationName,
                onValueChange = { medicationName = it },
                placeholder = { Text("e.g., Paracetamol") },
                leadingIcon = { Icon(Icons.Default.Medication, contentDescription = null, tint = Color.Gray) },
                trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White, focusedContainerColor = Color.White
                )
            )

            Spacer(Modifier.height(16.dp))

            // ---- TIME SELECTION ----
            Text(
                "Medication Time",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val context = LocalContext.current

            Column {
                times.forEachIndexed { idx, timeValue ->
                    OutlinedTextField(
                        value = timeValue,
                        onValueChange = {},
                        label = { Text("Time ${idx + 1}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .onFocusChanged { state ->
                                if (state.isFocused) {
                                    showTimePicker(
                                        context = context,
                                        initialTime = times[idx]
                                    ) { newTime ->
                                        times[idx] = newTime
                                    }
                                }
                            },
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                        trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )
                }
            }

            // Add / Remove Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (times.size < 5) {
                    TextButton(onClick = { times.add("08:00 AM") }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add time")
                    }
                }
                if (times.size > 1) {
                    TextButton(onClick = { times.removeAt(times.lastIndex) }) {
                        Icon(Icons.Default.Remove, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Remove")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Medication Dosage
            Text(
                "Medication Dosage",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                placeholder = { Text("e.g., 250 mg, 1 tablet, 5 ml") },
                leadingIcon = { Icon(Icons.Default.Medication, contentDescription = null, tint = BlueMain) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White, focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Medication Frequency
            Text(
                "Medication Frequency",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showFrequencyDialog = true },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray)
                        Spacer(Modifier.width(12.dp))
                        Text(frequency)
                    }
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Medication Duration (start / end) — open DatePickerDialogs
            Text(
                "Medication Duration",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // From Date
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showStartDatePicker = true },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("From", fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(startDate.format(dateFormatter), fontSize = 14.sp)
                            }
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // To Date
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEndDatePicker = true },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("To", fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(endDate.format(dateFormatter), fontSize = 14.sp)
                            }
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Take with Meal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Take with Meal?", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { beforeMeal = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (beforeMeal) BlueMain else Color.White,
                        contentColor = if (beforeMeal) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Before")
                }

                Button(
                    onClick = { beforeMeal = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!beforeMeal) BlueMain else Color.White,
                        contentColor = if (!beforeMeal) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("After")
                }
            }

            Spacer(Modifier.height(24.dp))

            // Add Medication Button
            Button(
                onClick = {
                    if (medicationName.isNotEmpty()) {
                        val primaryTime = times.firstOrNull() ?: "08:00 AM"
                        onSaveClick(
                            medicationName,
                            dosage,
                            frequency,
                            startDate,
                            endDate,
                            beforeMeal,
                            primaryTime,
                            "" // customInstruction removed — send empty string
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueMain),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Medication", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // --- Frequency dialog ---
    if (showFrequencyDialog) {
        val frequencyOptions = listOf(
            "Once a day",
            "Twice a day",
            "3x Per Week",
            "Every day",
            "Every other day"
        )

        AlertDialog(
            onDismissRequest = { showFrequencyDialog = false },
            title = { Text("Select Frequency") },
            text = {
                Column {
                    frequencyOptions.forEach { option ->
                        TextButton(
                            onClick = {
                                frequency = option
                                // times updated by LaunchedEffect(frequency)
                                showFrequencyDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(option, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFrequencyDialog = false }) { Text("Cancel") }
            }
        )
    }

    // --- Time options dialog (reusable) ---
    if (showTimeOptionsDialogForIndex != null) {
        val idx = showTimeOptionsDialogForIndex!!
        AlertDialog(
            onDismissRequest = { showTimeOptionsDialogForIndex = null },
            title = { Text("Select Time") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    timeOptions.forEach { time ->
                        TextButton(
                            onClick = {
                                if (idx in times.indices) {
                                    times[idx] = time
                                }
                                showTimeOptionsDialogForIndex = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(time, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimeOptionsDialogForIndex = null }) { Text("Cancel") }
            }
        )
    }

    // --- Start Date Picker (Android native DatePickerDialog for calendar UI) ---
    if (showStartDatePicker) {
        ShowAndroidDatePicker(
            context = context,
            initDate = startDate,
            onDateSelected = { picked ->
                startDate = picked
                // If end is earlier than start, adjust end
                if (endDate.isBefore(startDate)) {
                    endDate = startDate.plusDays(1)
                }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    // --- End Date Picker ---
    if (showEndDatePicker) {
        ShowAndroidDatePicker(
            context = context,
            initDate = endDate,
            onDateSelected = { picked ->
                endDate = picked
                // If end is before start, bump start
                if (endDate.isBefore(startDate)) {
                    startDate = endDate.minusDays(1)
                }
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

/**
 * Simple wrapper to show the Android DatePickerDialog and return a LocalDate.
 * Uses the platform DatePicker — provides the calendar UI familiar to Android users.
 */
@Composable
private fun ShowAndroidDatePicker(
    context: Context,
    initDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // Compose-friendly side-effect to show a single DatePickerDialog
    DisposableEffect(Unit) {
        val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val picked = LocalDate.of(year, month + 1, dayOfMonth) // month is 0-based in DatePickerDialog
            onDateSelected(picked)
        }

        val dialog = DatePickerDialog(
            context,
            listener,
            initDate.year,
            initDate.monthValue - 1,
            initDate.dayOfMonth
        )

        dialog.setOnCancelListener { onDismiss() }
        dialog.show()

        onDispose {
            dialog.dismiss()
        }
    }
}

fun showTimePicker(
    context: Context,
    initialTime: String?,
    onTimeSelected: (String) -> Unit
) {
    val cal = Calendar.getInstance()

    // Parse initial time if available ("08:00 AM")
    if (initialTime != null) {
        try {
            val sdf = java.text.SimpleDateFormat("hh:mm a", Locale.US)
            cal.time = sdf.parse(initialTime)!!
        } catch (_: Exception) { }
    }

    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)

    TimePickerDialog(
        context,
        { _, selHour, selMinute ->
            val chosenCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selHour)
                set(Calendar.MINUTE, selMinute)
            }

            val formatted = SimpleDateFormat("hh:mm a", Locale.US)
                .format(chosenCal.time)

            onTimeSelected(formatted)
        },
        hour,
        minute,
        false // << 12-hour format
    ).show()
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddMedicationScreenPreview() {
    AddMedicationScreen(
        onBackClick = {},
        onSaveClick = { _, _, _, _, _, _, _, _ -> }
    )
}
