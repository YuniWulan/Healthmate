package com.example.healthmateapp.screens

import androidx.compose.foundation.background
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
import java.time.LocalDate
import java.time.YearMonth

data class Reminder(
    val id: Int,
    val date: String,
    val time: String,
    val title: String,
    val iconColor: Color
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
    onInputBodyComposition: () -> Unit = {}
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now().dayOfMonth) }
    var calendarExpanded by remember { mutableStateOf(true) }
    var foodConsumptionExpanded by remember { mutableStateOf(true) }

    val foodItems = listOf(
        FoodItem("One Bowl of salad and salmon", 285),
        FoodItem("Oven Baked Chicken breast", 482)
    )

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
                        if (reminders.isNotEmpty()) {
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
            // Calendar Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
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

                        // Medication Reminders for today
                        if (reminders.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
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
                            reminders.forEach { reminder ->
                                ReminderItem(reminder = reminder, onClick = { onReminderClick(reminder) })
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // View All Medications Button
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

            // Health Metrics Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
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

                    // Health Metrics Grid
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

            // Daily Consumption Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
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
}

// Helper Composables
@Composable
fun ReminderItem(reminder: Reminder, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8F9FA))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(reminder.iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = reminder.title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = reminder.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = reminder.time,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        RadioButton(
            selected = false,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                unselectedColor = Color(0xFF0A84FF)
            )
        )
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
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
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
    HomeScreen()
}