package com.example.healthmateapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
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

// ------------------ MODEL ------------------
data class Medication(
    val id: String,
    val name: String,
    val note: String = "",
    val dosage: String = "",
    val time: String,
    var taken: Boolean = false
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
    onToggleTaken: (Medication, Boolean) -> Unit
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
                    onToggleTaken = { taken -> onToggleTaken(med, taken) },
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
    onToggleTaken: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GrayLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(med.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text("${med.note} • ${med.dosage}", fontSize = 13.sp, color = Color.Gray)
            }

            IconToggleButton(
                checked = med.taken,
                onCheckedChange = onToggleTaken
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (med.taken) BlueMain else Color(0xFFE4E4E4)),
                    contentAlignment = Alignment.Center
                ) {
                    if (med.taken) {
                        Icon(
                            Icons.Default.Check,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    // Month Picker Dialog
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
    onToggleTaken: (LocalDate, String, Boolean) -> Unit = { _, _, _ -> },
    onAddMedicationClick: () -> Unit = {}
) {
    var focusedDate by remember { mutableStateOf(LocalDate.now()) }
    var viewWeekly by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showMonthPicker by remember { mutableStateOf(false) }

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
                onNavigate = { route -> navController?.navigate(route) }
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
                            onToggleTaken = { med, taken ->
                                onToggleTaken(selectedDate, med.id, taken)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MedicationScheduleScreenPreview() {
    MedicationScheduleScreen()
}