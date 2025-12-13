package com.example.healthmateapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmateapp.viewmodel.AuthViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantProfileScreen(
    authViewModel: AuthViewModel = viewModel(),
    onHomeClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onAccountSettingsClick: () -> Unit = {},
    onPrivacySecurityClick: () -> Unit = {},
    onHelpSupportClick: () -> Unit = {}
) {

    val userName = authViewModel.userName.collectAsState().value ?: ""
    val userRole = "Caregiver"
    //val userRole = authViewModel.userRole.collectAsState().value ?: "Caregiver"


    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onHomeClick,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 12.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNotificationClick,
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Notification") },
                    label = { Text("Notification", fontSize = 12.sp) }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedTextColor = Color(0xFF0A84FF),
                        selectedIconColor = Color(0xFF0A84FF),
                        indicatorColor = Color(0xFF0A84FF).copy(alpha = 0.1f)
                    )
                )
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // ================= HEADER PROFILE MIRIP PATIENT =================
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(55.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = userName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = userRole, // BUKAN assistant → caregiver
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ================= MENU SECTION =================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {

                    MenuItem(
                        icon = Icons.Default.Settings,
                        title = "Account Settings",
                        onClick = onAccountSettingsClick
                    )

                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFE0E0E0)
                    )

                    MenuItem(
                        icon = Icons.Default.Lock,
                        title = "Privacy & Security",
                        onClick = onPrivacySecurityClick
                    )

                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFE0E0E0)
                    )

                    MenuItem(
                        icon = Icons.Default.Help,
                        title = "Help & Support",
                        onClick = onHelpSupportClick
                    )

                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFE0E0E0)
                    )

                    // LOGOUT → memanggil fungsi logout dari AuthViewModel
                    MenuItem(
                        icon = Icons.Default.ExitToApp,
                        title = "Log Out",
                        iconTint = Color(0xFFFF3B30),
                        titleColor = Color(0xFFFF3B30),
                        onClick = {
                            authViewModel.logout()
                            onLogoutClick()

                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
    title: String,
    iconTint: Color = Color(0xFF0A84FF),
    titleColor: Color = Color.Black,
    onClick: () -> Unit = {}
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            color = titleColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAssistantProfile() {
    AssistantProfileScreen()
}
