package com.example.healthmateapp.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmateapp.components.BottomNavigationBar
import com.example.healthmateapp.ui.theme.BlueMain
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign

// NOTE: Reminder now includes optional dosage & note so UI can show them.
// If other parts of your app depend on old Reminder, adapt accordingly.
data class Reminder(
    val id: Int,
    val date: String,
    val time: String,
    val title: String,
    val iconColor: Color,
    val dosage: String = "",
    val note: String = ""
)

data class FoodItem(
    val name: String,
    val calories: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String = "Jonathan",
    bloodPressureValue: String = "120/80",
    bloodGlucoseValue: String = "95",
    cholesterolValue: String = "180",
    bodyCompositionValue: String = "22.5",
    reminders: List<Reminder> = emptyList(),
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onReminderClick: (Reminder) -> Unit = {},
    onRecordConsumptionClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onBottomNavClick: (String) -> Unit = {},
    onInputBloodPressure: () -> Unit = {},
    onInputBloodGlucose: () -> Unit = {},
    onInputCholesterol: () -> Unit = {},
    onInputBodyComposition: () -> Unit = {},
    // NEW: callback to inform parent that reminder was confirmed / marked taken
    onConfirmTaken: (Reminder) -> Unit = {}
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now().dayOfMonth) }
    var calendarExpanded by remember { mutableStateOf(true) }
    var foodConsumptionExpanded by remember { mutableStateOf(true) }

    val foodItems = listOf(
        FoodItem("One Bowl of salad and salmon", 285),
        FoodItem("Oven Baked Chicken breast", 482)
    )

    val context = LocalContext.current

    val ctx = LocalContext.current

    // Local state to track which reminders are taken (so UI shows green check)
    val takenMap = remember { mutableStateMapOf<Int, Boolean>() }
    LaunchedEffect(reminders) {
        reminders.forEach { r ->
            takenMap.putIfAbsent(r.id, false)
        }
    }

    // For confirmation dialog
    var toConfirmReminder by remember { mutableStateOf<Reminder?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Hi, $userName",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = onNotificationClick) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.Black
                            )
                        }
                        if (reminders.any { !takenMap.getOrDefault(it.id, false) }) {
                            // show red dot when there are pending reminders
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .offset(x = 4.dp, y = 8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "home",
                onNavigate = onBottomNavClick
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Calendar Section (kept similar)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Calendar",
                                tint = Color(0xFF0A84FF),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Today's Medications",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "${reminders.size} medication${if (reminders.size != 1) "s" else ""} scheduled",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        IconButton(onClick = { calendarExpanded = !calendarExpanded }) {
                            Icon(
                                imageVector = if (calendarExpanded) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand/Collapse"
                            )
                        }
                    }

                    if (calendarExpanded) {
                        Spacer(modifier = Modifier.height(16.dp))

                        if (reminders.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No medications scheduled for today",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        } else {
                            // Show medication cards (same style as MedicationScheduleScreen)
                            reminders.forEach { reminder ->
                                MedicationRowCard(
                                    reminder = reminder,
                                    taken = takenMap.getOrDefault(reminder.id, false),
                                    onClickCard = { onReminderClick(reminder) },
                                    onCheckClick = {
                                        // open confirmation dialog
                                        toConfirmReminder = reminder
                                        showConfirmDialog = true
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { onBottomNavClick("reminder") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("View All Medications")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Health Metrics (unchanged)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Health Metrics",
                                tint = Color(0xFF0A84FF),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Health Metrics",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Your health indicators",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HealthMetricCard(
                            icon = Icons.Default.Favorite,
                            iconColor = Color(0xFFFF6B6B),
                            title = "Blood Pressure",
                            value = bloodPressureValue,
                            unit = "mmHg",
                            onInputClick = onInputBloodPressure,
                            modifier = Modifier.weight(1f)
                        )

                        HealthMetricCard(
                            icon = Icons.Default.Star,
                            iconColor = Color(0xFFFFA500),
                            title = "Blood Glucose",
                            value = bloodGlucoseValue,
                            unit = "mg/dL",
                            onInputClick = onInputBloodGlucose,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HealthMetricCard(
                            icon = Icons.Default.Science,
                            iconColor = Color(0xFF4CAF50),
                            title = "Cholesterol",
                            value = cholesterolValue,
                            unit = "mg/dL",
                            onInputClick = onInputCholesterol,
                            modifier = Modifier.weight(1f)
                        )

                        HealthMetricCard(
                            icon = Icons.Default.FitnessCenter,
                            iconColor = Color(0xFF2196F3),
                            title = "Body Composition",
                            value = bodyCompositionValue,
                            unit = "% Fat",
                            onInputClick = onInputBodyComposition,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Consumption Section (unchanged)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Food",
                                tint = Color(0xFF0A84FF),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Daily Consumption",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Your daily food consumption",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        IconButton(onClick = { foodConsumptionExpanded = !foodConsumptionExpanded }) {
                            Icon(
                                imageVector = if (foodConsumptionExpanded) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand/Collapse"
                            )
                        }
                    }

                    if (foodConsumptionExpanded) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Food Consumption",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        foodItems.forEach { item ->
                            FoodConsumptionItem(item = item)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        OutlinedButton(
                            onClick = onRecordConsumptionClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "+ Record Your Consumptions",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog (simple, local) — shows when user taps check button on a med card
    if (showConfirmDialog && toConfirmReminder != null) {
        val r = toConfirmReminder!!
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false; toConfirmReminder = null },
            title = { Text("Confirm Medication") },
            text = {
                Column {
                    Text("Have you taken \"${r.title}\" at ${r.time}?")
                    if (r.dosage.isNotEmpty()) Text("Dosage: ${r.dosage}")
                    if (r.note.isNotEmpty()) Text("Note: ${r.note}")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    takenMap[r.id] = true
                    onConfirmTaken(r)
                    showConfirmDialog = false
                    toConfirmReminder = null
                    Toast.makeText(ctx, "Marked as taken", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Yes, I've taken it")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    toConfirmReminder = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * MedicationRowCard
 * Reusable single-row card for a reminder that mirrors MedicationScheduleScreen styling.
 */
@Composable
fun MedicationRowCard(
    reminder: Reminder,
    taken: Boolean,
    onClickCard: () -> Unit,
    onCheckClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = if (taken) Color(0xFFE8F5E9) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClickCard() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(reminder.iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (taken) Color(0xFF4CAF50) else Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = reminder.time, fontSize = 13.sp, color = Color.Gray)
                    if (reminder.dosage.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "• ${reminder.dosage}", fontSize = 13.sp, color = Color.Gray)
                    }
                }
                if (reminder.note.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = reminder.note, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (!taken) {
                Box() {
                    Icon(
                        imageVector = Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = "Not taken yet",
                        tint = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onCheckClick() },
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF4CAF50).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Taken",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

        }
    }
}

@Composable
fun FoodConsumptionItem(item: FoodItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = item.name,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${item.calories} Kcal",
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HealthMetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    unit: String,
    onInputClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        text = unit,
                        fontSize = 10.sp,
                        color = Color.Gray,
                    )
                }
                Spacer(modifier = Modifier.height(0.5.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            Button(
                onClick = onInputClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = iconColor
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Input",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val sample = listOf(
        Reminder(1, "12 Dec 2025", "08:00 AM", "Paracetamol", Color(0xFF0A84FF), dosage = "500mg", note = "After Meal"),
        Reminder(2, "12 Dec 2025", "12:00 PM", "Metformin", Color(0xFFFF8C42), dosage = "1 tablet", note = "Before Meal")
    )
    HomeScreen(
        reminders = sample,
        onConfirmTaken = { /* preview action */ }
    )
}
