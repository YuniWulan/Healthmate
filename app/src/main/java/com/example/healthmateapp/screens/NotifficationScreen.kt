package com.example.healthmateapp.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// -----------------------------------------------------------
// DATA
// -----------------------------------------------------------

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val icon: ImageVector,
    val color: Color,
    val isRead: Boolean,
    val category: NotificationCategory
)

enum class NotificationCategory {
    NEWEST,
    LATER,
    LAST_WEEK
}

// -----------------------------------------------------------
// SCREEN
// -----------------------------------------------------------

@Composable
fun NotificationScreen() {

    val notifications = remember {
        mutableStateListOf(
            NotificationItem(
                id = "1",
                title = "Unread AI Chatbot Message",
                message = "Doc AI just sent you 51 messages!",
                icon = Icons.Default.Chat,
                color = Color(0xFF5570FF),
                isRead = false,
                category = NotificationCategory.NEWEST
            ),
            NotificationItem(
                id = "2",
                title = "Activity Completed",
                message = "You have finished jogging.",
                icon = Icons.Default.DirectionsRun,
                color = Color(0xFF8B51FF),
                isRead = true,
                category = NotificationCategory.LATER
            ),
            NotificationItem(
                id = "3",
                title = "Monthly Health Insight",
                message = "Your monthly health insight is ready.",
                icon = Icons.Default.HealthAndSafety,
                color = Color(0xFF2AB38E),
                isRead = true,
                category = NotificationCategory.LAST_WEEK
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE7E8F1))
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ArrowBack, null)
            Text("Notifications", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.Settings, null)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -------- NEWEST --------
        Section("Newest") {
            notifications.filter { it.category == NotificationCategory.NEWEST }.forEachIndexed { index, item ->
                NotificationCard(item = item) {
                    notifications[index] = item.copy(isRead = true)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -------- LATER --------
        Section("Later") {
            notifications.filter { it.category == NotificationCategory.LATER }.forEachIndexed { index, item ->
                NotificationCard(item = item) {
                    notifications[index] = item.copy(isRead = true)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -------- LAST WEEK --------
        Section("Last Week") {
            notifications.filter { it.category == NotificationCategory.LAST_WEEK }.forEach { item ->
                NotificationCard(item = item) {}
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

// -----------------------------------------------------------
// COMPONENTS
// -----------------------------------------------------------

@Composable
fun Section(title: String, content: @Composable () -> Unit) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
    Spacer(modifier = Modifier.height(8.dp))
    content()
}

@Composable
fun NotificationCard(item: NotificationItem, onClick: () -> Unit) {

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
                tint = Color.White.copy(alpha = if (item.isRead) 0.7f else 1f),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(item.title, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                item.message,
                fontSize = 13.sp,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScreen() {
    NotificationScreen()
}
