package com.example.healthmateapp.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            label = { Text("Home", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0A84FF),
                selectedTextColor = Color(0xFF0A84FF),
                indicatorColor = Color(0xFF0A84FF).copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "reminder",
            onClick = { onNavigate("reminder") },
            icon = { Icon(Icons.Default.DateRange, "Reminder") },
            label = { Text("Reminder", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0A84FF),
                selectedTextColor = Color(0xFF0A84FF),
                indicatorColor = Color(0xFF0A84FF).copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "chat",
            onClick = { onNavigate("chat") },
            icon = { Icon(Icons.Default.Chat, "Chat") },
            label = { Text("AI Chat", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0A84FF),
                selectedTextColor = Color(0xFF0A84FF),
                indicatorColor = Color(0xFF0A84FF).copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "account",
            onClick = { onNavigate("account") },
            icon = { Icon(Icons.Default.Person, "Account") },
            label = { Text("Account", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0A84FF),
                selectedTextColor = Color(0xFF0A84FF),
                indicatorColor = Color(0xFF0A84FF).copy(alpha = 0.1f)
            )
        )
    }
}