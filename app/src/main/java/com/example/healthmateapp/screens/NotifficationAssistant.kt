package com.example.healthmateapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =======================================================
// DATA
// =======================================================
data class AssistantNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val icon: ImageVector,
    val color: Color,
    val isRead: Boolean
)

// =======================================================
// MAIN SCREEN
// =======================================================
@Composable
fun NotificationAssistantScreen(
    onHomeClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {

    val notifications = remember {
        mutableStateListOf(
            AssistantNotificationItem(
                id = "1",
                title = "Medication Reminder",
                message = "Patient Michael Hart had not taken his morning medication.",
                icon = Icons.Default.Notifications,
                color = Color(0xFF5570FF),
                isRead = false
            ),
            AssistantNotificationItem(
                id = "2",
                title = "Control Schedule",
                message = "Patient Daniel Ross needs blood pressure monitoring.",
                icon = Icons.Default.Notifications,
                color = Color(0xFF8B51FF),
                isRead = true
            ),
            AssistantNotificationItem(
                id = "3",
                title = "Activity Record",
                message = "Patient Laura Kim completed her exercise routine.",
                icon = Icons.Default.Notifications,
                color = Color(0xFF2AB38E),
                isRead = true
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE7E8F1))
            .padding(horizontal = 16.dp)
            .padding(top = 64.dp) // dikurangi karena top bar dihapus
    ) {

        // -------------------------------
        // LIST
        // -------------------------------
        notifications.forEachIndexed { index, item ->
            NotificationAssistantCard(
                item = item,
                onClick = {
                    notifications[index] = item.copy(isRead = true)
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // -------------------------------
        // BOTTOM NAVIGATION BAR
        // -------------------------------
        NotificationBottomBar(
            onProfileClick = onProfileClick,
            onNotificationClick = onNotificationClick
        )
    }
}

// =======================================================
// CARD COMPONENT
// =======================================================
@Composable
fun NotificationAssistantCard(
    item: AssistantNotificationItem,
    onClick: () -> Unit
) {

    val bgColor = if (item.isRead) Color(0xFFF1F2F5) else Color.White
    val textColor = if (item.isRead) Color.Black.copy(alpha = 0.75f) else Color.Black

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(16.dp)
    ) {

        // ICON BOX
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(item.color.copy(alpha = if (item.isRead) 0.4f else 1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = if (item.isRead) 0.75f else 1f),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.message,
                fontSize = 13.sp,
                color = textColor.copy(alpha = 0.85f)
            )
        }
    }
}

// =======================================================
// BOTTOM BAR
// =======================================================
@Composable
fun NotificationBottomBar(
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        // HOME
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 12.sp) },
        )

        // NOTIFICATION
        NavigationBarItem(
            selected = true,
            onClick = onNotificationClick,
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Notification") },
            label = { Text("Notification", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF0A84FF),
            selectedTextColor = Color(0xFF0A84FF),
            indicatorColor = Color(0xFF0A84FF).copy(alpha = 0.1f)
             )
            )

        // PROFILE
        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 12.sp) }
        )
    }
}

// =======================================================
// PREVIEW
// =======================================================
@Preview(showBackground = true)
@Composable
fun NotifficationAssistantScreenPreview() {
    NotificationAssistantScreen()
}
