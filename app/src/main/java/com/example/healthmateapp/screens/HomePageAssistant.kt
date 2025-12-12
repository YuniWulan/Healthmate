package com.example.healthmateapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmateapp.viewmodel.AssistantViewModel
import com.example.healthmateapp.viewmodel.Patient
import com.example.healthmateapp.Screen
import androidx.navigation.NavController

// ------------------------------------------------------------
// DATA CLASS
// ------------------------------------------------------------
data class AssistantPatient(
    val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val illness: String = ""
)

// ------------------------------------------------------------
// MAIN SCREEN
// ------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenAssistant(
    navController: NavController,
    viewModel: AssistantViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    assistantName: String,
    onPatientClick: (AssistantPatient) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {

    // ------------------------------------------------------------
    // 🔥 ORIGINAL VIEWMODEL FETCH (DISIMPAN, HANYA DI-COMMENT)
    // ------------------------------------------------------------
    /*
    val patientsState = remember { mutableStateOf<List<Patient>>(emptyList()) }

    LaunchedEffect(Unit) {
        val observer = androidx.lifecycle.Observer<List<Patient>> { list ->
            patientsState.value = list
        }
        viewModel.patientList.observeForever(observer)
        viewModel.loadPatients()
    }

    val assistantPatients = patientsState.value.map { patient ->
        AssistantPatient(
            id = patient.id,
            name = patient.username,
            age = patient.age,
            illness = patient.illness
        )
    }
    */

    // ------------------------------------------------------------
    // 🔥 DUMMY PATIENT LIST (BIAR UI JALAN)
    // ------------------------------------------------------------
    val assistantPatients = listOf(
        AssistantPatient("1", "Michael Hart", 54, "Hypertension"),
        AssistantPatient("2", "Daniel Ross", 48, "Heart Disease"),
        AssistantPatient("3", "Laura Kim", 29, "Diabetes Type 2")
    )

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
                                color = Color.Black
                            )
                            Text(
                                text = "Caregiver",
                                fontSize = 14.sp,
                                color = Color.Gray
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

        // ------------------------------------------------------------
        // LIST CONTENT
        // ------------------------------------------------------------
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {
                Text(
                    text = "Your Patient List",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(assistantPatients) { patient ->
                AssistantPatientCard(patient = patient) {
                    // Navigasi ke placeholder screen
                    navController.navigate(Screen.PatientDetails.route)
                }
            }

            // Spacer agar tidak ketutup nav bar
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ------------------------------------------------------------
// PATIENT CARD
// ------------------------------------------------------------
@Composable
fun AssistantPatientCard(patient: AssistantPatient, onSeeDetailsClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

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

                Text(
                    patient.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text("Age", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${patient.age}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(Color.LightGray)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text("History of Illness", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(patient.illness, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSeeDetailsClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0066CC),
                    contentColor = Color.White
                )
            ) {
                Text("See Details", fontSize = 12.sp)
            }
        }
    }
}

// ------------------------------------------------------------
// BOTTOM NAVIGATION
// ------------------------------------------------------------
@Composable
fun AssistantBottomBar(
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {

        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Default.Home, "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedTextColor = Color(0xFF0A84FF),
                selectedIconColor = Color(0xFF0A84FF),
                indicatorColor = Color(0xFF0A84FF).copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = onNotificationClick,
            icon = { Icon(Icons.Default.Notifications, "Notification") },
            label = { Text("Notification") }
        )

        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = { Icon(Icons.Default.Person, "Profile") },
            label = { Text("Profile") }
        )
    }
}

// ------------------------------------------------------------
// PREVIEW
// ------------------------------------------------------------
//@Preview(showBackground = true)
//@Composable
//fun HomeScreenAssistantPreview() {
  //  HomeScreenAssistant(
    //    assistantName = "Preview Assistant"
    //)
//}
