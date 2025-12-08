package com.example.healthmateapp.screens


import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
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
import com.example.healthmateapp.screens.components.MonthGrid
import com.example.healthmateapp.screens.components.WeekStrip
import com.example.healthmateapp.ui.theme.BgGray
import com.example.healthmateapp.ui.theme.BlueMain
import com.example.healthmateapp.ui.theme.GrayLight
import java.time.DayOfWeek
import java.time.LocalDate
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.draw.drawBehind
import java.time.format.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextOverflow
import java.util.Locale

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

private fun sampleSchedule(): MutableMap<LocalDate, MutableList<Medication>> {
    val map = mutableMapOf<LocalDate, MutableList<Medication>>()

    val today = LocalDate.now()
    val monday = startOfWeek(today)

    val dummyList = listOf(
        // Monday
        Medication("m1", "Amoxicillin", "Before Eating", "250 mg", "07:00 AM"),
        Medication("m2", "Vitamin C", "After Eating", "1000 mg", "08:00 AM"),

        // Tuesday
        Medication("m3", "Paracetamol", "After Eating", "500 mg", "12:00 PM"),
        Medication("m4", "Test", "Before Eating", "250 mg", "09:00 AM"),

        // Wednesday
        Medication("m5", "Ibuprofen", "If pain", "400 mg", "01:00 PM"),
        Medication("m6", "Cetirizine", "Before Bed", "10 mg", "09:00 PM"),

        // Thursday
        Medication("m7", "Omeprazole", "Before Eating", "20 mg", "06:00 AM"),
        Medication("m8", "Albuterol", "If needed", "1 mg", "09:00 AM"),

        // Friday
        Medication("m9", "Metformin", "After Eating", "500 mg", "07:00 AM"),
        Medication("m10", "Ranitidine", "Before Eating", "150 mg", "08:00 AM"),

        // Saturday
        Medication("m11", "Zinc", "After Eating", "30 mg", "10:00 AM"),
        Medication("m12", "Magnesium", "Before Bed", "200 mg", "09:00 PM"),

        // Sunday
        Medication("m13", "Calcium", "After Eating", "600 mg", "11:00 AM"),
        Medication("m14", "Probiotic", "Before Eating", "1 capsule", "07:00 AM")
    )

    for (i in 0 until 7) {
        val date = monday.plusDays(i.toLong())
        val start = i * 2
        val end = start + 2

        map[date] = dummyList.subList(start,end).toMutableList()
    }
    return map
}

private fun groupByTime(meds: List<Medication>): Map<String, List<Medication>> =
    meds.groupBy { it.time }

// ------------------ COMPONENTS ------------------

@Composable
fun BottomNavigationBar(
    currentRoute: String = "home",
    onNavigate: (String) -> Unit = {}
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            icon = { Icon(Icons.Default.Home, "Home") },
            label = { Text("Home", fontSize = 12.sp) }
        )
        NavigationBarItem(
            selected = currentRoute == "reminder",
            onClick = { onNavigate("reminder") },
            icon = { Icon(Icons.Default.DateRange, "Reminder") },
            label = { Text("Reminder", fontSize = 12.sp) }
        )
        NavigationBarItem(
            selected = currentRoute == "chat",
            onClick = { onNavigate("chat") },
            icon = { Icon(Icons.AutoMirrored.Filled.Chat, "Chat") },
            label = { Text("Chat", fontSize = 12.sp) }
        )
        NavigationBarItem(
            selected = currentRoute == "account",
            onClick = { onNavigate("account") },
            icon = { Icon(Icons.Default.Person, "Account") },
            label = { Text("Account", fontSize = 12.sp) }
        )
    }
}

@Composable
fun TimelineItem(
    time: String,
    totalItems: Int,
    content: @Composable () -> Unit
) {

    val dotSize = 14.dp
    val dotToTextGap = 6.dp
    val lineWidth = 2.dp
    val lineTopGap = 4.dp

    Layout(
        modifier = Modifier.drawBehind {

            val dotPx = dotSize.toPx()
            val linePx = lineWidth.toPx()
            val gapPx = lineTopGap.toPx()

            // Garis tepat di tengah dot
            val lineLeft = (dotPx / 2f) - (linePx / 2f)

            val startY = dotPx + gapPx
            val endY = size.height

            drawRect(
                color = Color.Black.copy(alpha = 0.12f),
                topLeft = Offset(lineLeft, startY),
                size = Size(linePx, (endY - startY).coerceAtLeast(0f))
            )
        },
        content = {

            // ROW HEAD
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

            Column {
                Spacer(Modifier.height(32.dp))
                content()
            }
        }
    ) { measurables, constraints ->

        val left = measurables[0].measure(constraints)
        val right = measurables[1].measure(constraints)

        val totalHeight = maxOf(left.height, right.height)

        layout(constraints.maxWidth, totalHeight) {
            left.place(0, 0)
            right.place(left.width - 328.dp.roundToPx(), 0)
        }
    }
}


@Composable
fun MedicationCard(med: Medication, onToggleTaken: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        .size(15.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (med.taken) BlueMain else Color(0xFFE4E4E4)),
                    contentAlignment = Alignment.Center
                ) {
                    if (med.taken) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
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

// ------------------ SCREEN ------------------

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MedicationScheduleScreen(
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    val schedule = remember { sampleSchedule() }

    var focusedDate by remember { mutableStateOf(LocalDate.now()) }
    var viewWeekly by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val medsForSelected by remember {
        derivedStateOf { schedule[selectedDate] ?: emptyList() }
    }
    val grouped by remember(medsForSelected) {
        derivedStateOf { groupByTime(medsForSelected) }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "reminder",
                onNavigate = { route -> navController?.navigate(route) }
            )
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

            if (viewWeekly) {
                WeekStrip(
                    weekStart = startOfWeek(focusedDate),
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
                Text("Tidak ada jadwal obat pada hari ini", color = Color.Gray)
            } else {
                LazyColumn {
                    grouped.forEach { (time, meds) ->

                        val totalThisHour = meds.size

                        items(meds) { med ->
                            TimelineItem(
                                time = time,
                                totalItems = totalThisHour
                            ){
                                MedicationCard(
                                    med = med,
                                    onToggleTaken = { med.taken = it }
                                )
                            }
                        }

                        item { Spacer(Modifier.height(12.dp)) }
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
