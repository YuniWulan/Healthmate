package com.example.healthmateapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// -----------------------------------------\-------------------
// DATA
// ------------------------------------------------------------

data class AssistantPatient(
    val id: Int,
    val name: String,
    val age: Int,
    val illness: String
)

// ------------------------------------------------------------
// MAIN SCREEN
// ------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenAssistant(
    assistantName: String = "Sarah",
    patients: List<AssistantPatient> = sampleAssistantPatients(),
    onPatientClick: (AssistantPatient) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {

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

                        Column {
                            Text(
                                text = "Hi, $assistantName",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                lineHeight = 15.sp
                            )
                            Text(
                                text = "Caregiver",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                lineHeight = 3.sp
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
            AssistantBottomBar(
                onProfileClick = onProfileClick,
                onNotificationClick = onNotificationClick
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
                .padding(16.dp)
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your Patient List",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 28.dp)
            )

            patients.forEach { patient ->
                AssistantPatientCard(patient = patient) {
                    onPatientClick(patient)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}


// ------------------------------------------------------------
// CARD — PATIENT ITEM
// ------------------------------------------------------------

@Composable
fun AssistantPatientCard(patient: AssistantPatient, onClick: () -> Unit) {

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {

            // FOTO + NAMA
            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE6E6E6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .height(25.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        patient.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp),
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {

                // ===================== UMUR =====================
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                ) {

                    // LABEL DALAM BOX KECIL
                    Box(
                        modifier = Modifier
                            .height(15.dp)
                            .width(48.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            "Age",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // VALUE BOX
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            "${patient.age}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }
                }

                // GARIS PEMISAH
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(65.dp)
                        .background(Color.LightGray)
                )

                // ===================== RIWAYAT PENYAKIT =====================
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                ) {

                    // LABEL DALAM BOX KECIL
                    Box(
                        modifier = Modifier
                            .height(15.dp)
                            .width(160.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            "History Of Illnes",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // VALUE BOX
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            patient.illness,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0066CC),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(vertical = 0.dp)
            ) {
                Text("See Details", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ------------------------------------------------------------
// BOTTOM NAV
// ------------------------------------------------------------

@Composable
fun AssistantBottomBar(
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit )
{
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
) {
    NavigationBarItem(
        selected = true,
        onClick = { },
        icon = { Icon(Icons.Default.Home, "Home") },
        label = { Text("Home", fontSize = 12.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF0A84FF),
            selectedTextColor = Color(0xFF0A84FF),
            indicatorColor = Color(0xFF0A84FF).copy(alpha = 0.1f)
        )
    )
        NavigationBarItem(
            selected = false,
            onClick = onNotificationClick,
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Notiffication") },
            label = { Text("Notiffication", fontSize = 12.sp) }
        )

        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 12.sp) }
        )
    }
}


// ------------------------------------------------------------
// SAMPLE DATA
// ------------------------------------------------------------

fun sampleAssistantPatients(): List<AssistantPatient> = listOf(
    AssistantPatient(1, "Michael Hart", 72, "Diabetes"),
    AssistantPatient(2, "Laura Kim", 65, "Hypertension"),
    AssistantPatient(3, "Daniel Ross", 80, "Heart Disease"),
)

@Preview(showBackground = true)
@Composable
fun HomeScreenAssistantPreview() {
    HomeScreenAssistant()
}
