package com.example.healthmateapp.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import java.time.LocalDate
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Arrangement
import com.example.healthmateapp.ui.theme.BlueMain

@Composable
fun MonthStrip(
    month: LocalDate,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit
) {
    val firstOfMonth = month.withDayOfMonth(1)
    val daysInMonth = month.lengthOfMonth()

    val monthDays = remember(month) {
        List(daysInMonth) { index -> firstOfMonth.plusDays(index.toLong()) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        monthDays.forEach { date ->
            val isSelected = date == selectedDate

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) BlueMain else Color.White)
                    .clickable { onSelectDate(date) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = date.dayOfWeek.name.take(3), // MON, TUE, WED
                    fontSize = 12.sp,
                    color = if (isSelected) Color.White else Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color.Black
                )
            }
        }
    }
}
