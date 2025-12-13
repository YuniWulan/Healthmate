package com.example.healthmateapp.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.healthmateapp.components.BottomNavigationBar
import com.example.healthmateapp.screens.components.MonthGrid
import com.example.healthmateapp.screens.components.WeekStrip
import com.example.healthmateapp.screens.medication.MedicationConfirmationDialog
import com.example.healthmateapp.ui.theme.BgGray
import com.example.healthmateapp.ui.theme.BlueMain
import com.example.healthmateapp.ui.theme.GrayLight
import java.time.DayOfWeek
import java.time.LocalDate
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.Size
import com.example.healthmateapp.screens.components.MonthStrip
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

// ------------------ MODEL ------------------
data class Medication(
    val id: String,
    val name: String,
    val note: String = "",
    val dosage: String = "",
    val time: String,
    var taken: Boolean = false,
    val takenDetails: TakenDetails? = null
)

data class TakenDetails(
    val day: String,
    val time: String,
    val date: String,
    val notes: String,
    val photoUri: Uri?
)

// ------------------ HELPERS ------------------
private fun startOfWeek(date: LocalDate): LocalDate {
    val diff = (date.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    return date.minusDays(diff.toLong())
}

private fun groupByTime(meds: List<Medication>): List<Pair<String, List<Medication>>> {
    return meds.groupBy { it.time }
        .toList()
        .sortedBy { it.first }
}

// ------------------ COMPONENTS ------------------

@Composable
fun TimelineItem(
    time: String,
    totalItems: Int,
    medications: List<Medication>,
    onToggleTaken: (Medication) -> Unit,
    onEdit: (Medication) -> Unit,
    onDelete: (Medication) -> Unit
) {
    val dotSize = 14.dp
    val dotToTextGap = 12.dp
    val lineWidth = 2.dp
    val lineTopGap = 4.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val dotPx = dotSize.toPx()
                val linePx = lineWidth.toPx()
                val gapPx = lineTopGap.toPx()

                val lineLeft = (dotPx / 2f) - (linePx / 2f)

                val startY = dotPx + gapPx
                val endY = size.height

                drawRect(
                    color = Color.Black.copy(alpha = 0.12f),
                    topLeft = Offset(lineLeft, startY),
                    size = Size(linePx, (endY - startY).coerceAtLeast(0f))
                )
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(3.dp))
            )

            Spacer(Modifier.width(dotToTextGap))

            Text(
                text = time,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = "$totalItems Total",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(Modifier.height(16.dp))

        medications.forEachIndexed { index, med ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.width(dotSize + dotToTextGap))

                MedicationCard(
                    med = med,
                    onToggleTaken = { onToggleTaken(med) },
                    onEdit = { onEdit(med) },
                    onDelete = { onDelete(med) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (index < medications.size - 1) {
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun MedicationCard(
    med: Medication,
    onToggleTaken: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (med.taken) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    med.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = if (med.taken) Color(0xFF4CAF50) else Color.Black
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${med.note} • ${med.dosage}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                // Show taken details if medication was taken
                if (med.taken && med.takenDetails != null) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Taken at ${med.takenDetails.time}",
                            fontSize = 11.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Show if photo proof was added
                    if (med.takenDetails.photoUri != null) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = BlueMain,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Photo attached",
                                fontSize = 11.sp,
                                color = BlueMain,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Action buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Toggle button (only show if not taken)
                if (!med.taken) {
                    IconButton(
                        onClick = onToggleTaken,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Transparent, RoundedCornerShape(6.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .border(
                                    width = 2.dp,
                                    color = Color.Gray.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.width(4.dp))

                // Menu button
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.Gray
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = BlueMain,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Edit")
                                }
                            },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Delete", color = Color(0xFFD32F2F))
                                }
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    medicationName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(32.dp)
            )
        },
        title = { Text("Delete Medication?") },
        text = {
            Text("Are you sure you want to delete \"$medicationName\"? This action cannot be undone.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CalendarHeader(
    focusedDate: LocalDate,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    viewWeekly: Boolean,
    onToggleView: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .shadow(2.dp)
            .background(Color.White)
            .padding(8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev) {
                Icon(Icons.Default.KeyboardArrowLeft, null)
            }
            IconButton(onClick = onToggleView) {
                Icon(Icons.Default.CalendarToday, null)
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.KeyboardArrowRight, null)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MedicationScheduleScreen(
    navController: NavController? = null,
    modifier: Modifier = Modifier,
    medications: Map<LocalDate, List<Medication>> = emptyMap(),
    onToggleTaken: (LocalDate, String, Boolean) -> Unit,
    onEditMedication: (LocalDate, String) -> Unit = { _, _ -> },
    onDeleteMedication: (LocalDate, String) -> Unit = { _, _ -> },
    onAddMedicationClick: () -> Unit = {}
) {
    var focusedDate by remember { mutableStateOf(LocalDate.now()) }
    var viewWeekly by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var selectedMedication by remember { mutableStateOf<Medication?>(null) }
    var medicationToDelete by remember { mutableStateOf<Medication?>(null) }

    val context = LocalContext.current

    // Update week start when focusedDate or selectedDate changes
    val weekStart by remember(focusedDate, selectedDate) {
        derivedStateOf {
            if (viewWeekly) {
                startOfWeek(selectedDate)
            } else {
                startOfWeek(focusedDate)
            }
        }
    }

    val medsForSelected by remember(selectedDate, medications) {
        derivedStateOf { medications[selectedDate] ?: emptyList() }
    }
    val grouped by remember(medsForSelected) {
        derivedStateOf { groupByTime(medsForSelected) }
    }

    // Month list for picker
    val monthFormatter = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "reminder",
                onNavigate = { route ->
                    when (route) {
                        "home" -> {
                            navController?.navigate("home") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                        "reminder" -> {
                            // Already on reminder page, do nothing
                        }
                        "chat" -> {
                            Toast.makeText(context, "Chat feature coming soon", Toast.LENGTH_SHORT).show()
                        }
                        "account" -> {
                            navController?.navigate("profile") {
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMedicationClick,
                containerColor = BlueMain,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Medication")
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(BgGray)
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "My Medications",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            // Month Selector Dropdown
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMonthPicker = true },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = BlueMain,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = focusedDate.format(monthFormatter),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Tap to change month",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select Month",
                        tint = Color.Gray
                    )
                }
            }

            if (showMonthPicker) {
                // Pre-calculate month list outside of dialog to avoid recomposition issues
                val previousMonths = remember(focusedDate) {
                    List(3) { i -> focusedDate.minusMonths((3 - i).toLong()) }
                }
                val nextMonths = remember(focusedDate) {
                    List(6) { i -> focusedDate.plusMonths((i + 1).toLong()) }
                }

                AlertDialog(
                    onDismissRequest = { showMonthPicker = false },
                    title = { Text("Select Month") },
                    text = {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 400.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Previous months
                            previousMonths.forEach { date ->
                                TextButton(
                                    onClick = {
                                        focusedDate = date
                                        selectedDate = date.withDayOfMonth(1)
                                        showMonthPicker = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        date.format(monthFormatter),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // Current month
                            TextButton(
                                onClick = {
                                    val now = LocalDate.now()
                                    focusedDate = now
                                    selectedDate = now
                                    showMonthPicker = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    LocalDate.now().format(monthFormatter) + " (Current)",
                                    modifier = Modifier.fillMaxWidth(),
                                    fontWeight = FontWeight.Bold,
                                    color = BlueMain
                                )
                            }

                            // Next months
                            nextMonths.forEach { date ->
                                TextButton(
                                    onClick = {
                                        focusedDate = date
                                        selectedDate = date.withDayOfMonth(1)
                                        showMonthPicker = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        date.format(monthFormatter),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showMonthPicker = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            if (viewWeekly) {
                MonthStrip(
                    month = focusedDate,
                    selectedDate = selectedDate,
                    onSelectDate = { selectedDate = it }
                )
            } else {
                CalendarHeader(
                    focusedDate = focusedDate,
                    onPrev = { focusedDate = focusedDate.minusMonths(1) },
                    onNext = { focusedDate = focusedDate.plusMonths(1) },
                    viewWeekly = viewWeekly,
                    onToggleView = { viewWeekly = !viewWeekly }
                )

                Spacer(Modifier.height(12.dp))

                MonthGrid(
                    monthFocused = focusedDate,
                    selectedDate = selectedDate,
                    onSelectDate = { selectedDate = it }
                )
            }

            Spacer(Modifier.height(18.dp))

            Text("${medsForSelected.size} Total", fontSize = 13.sp, color = Color.Gray)

            Spacer(Modifier.height(12.dp))

            if (medsForSelected.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.MedicalServices,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No medication schedule for this day",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onAddMedicationClick,
                        colors = ButtonDefaults.buttonColors(containerColor = BlueMain)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Medication")
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(grouped) { (time, meds) ->
                        TimelineItem(
                            time = time,
                            totalItems = meds.size,
                            medications = meds,
                            onToggleTaken = { med ->
                                // Only show dialog if medication is not yet taken
                                if (!med.taken) {
                                    selectedMedication = med
                                }
                            },
                            onEdit = { med ->
                                onEditMedication(selectedDate, med.id)
                            },
                            onDelete = { med ->
                                medicationToDelete = med
                            }
                        )
                    }
                }
            }
        }

        // Show confirmation dialog when medication is selected
        selectedMedication?.let { medication ->
            MedicationConfirmationDialog(
                medicationName = medication.name,
                medicationDosage = medication.dosage,
                medicationNote = medication.note,
                onDismiss = { selectedMedication = null },
                onConfirm = { day, time, date, notes, photoUri ->
                    val takenDetails = TakenDetails(
                        day = day,
                        time = time,
                        date = date,
                        notes = notes,
                        photoUri = photoUri
                    )
                    onToggleTaken(selectedDate, medication.id, true)
                    selectedMedication = null
                }
            )
        }

        // Show delete confirmation dialog
        medicationToDelete?.let { medication ->
            DeleteConfirmationDialog(
                medicationName = medication.name,
                onDismiss = { medicationToDelete = null },
                onConfirm = {
                    onDeleteMedication(selectedDate, medication.id)
                    medicationToDelete = null
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MedicationSchedulePreview() {
    MedicationScheduleScreen(
        medications = emptyMap(),
        onToggleTaken = { _, _, _ -> },
        onEditMedication = { _, _ -> },
        onDeleteMedication = { _, _ -> },
        onAddMedicationClick = {}
    )
}