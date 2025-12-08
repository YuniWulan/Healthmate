package com.example.healthmateapp.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

@Composable
fun MonthGrid(
    monthFocused: LocalDate,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate) -> Unit
) {
    val firstDayOfMonth = monthFocused.withDayOfMonth(1)
    val daysInMonth = monthFocused.lengthOfMonth()

    // offset sebelum tanggal 1 (Monday = 0)
    val firstDayOffset = firstDayOfMonth.dayOfWeek.value - 1

    Column(modifier = Modifier.fillMaxWidth()) {

        // Header hari Senin–Minggu
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { dow ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val totalCells = firstDayOffset + daysInMonth

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            userScrollEnabled = false,
            modifier = Modifier.heightIn(min = 240.dp)
        ) {
            itemsIndexed(List(totalCells) { it }) { index, _ ->

                // Kotak sebelum tanggal 1 → kosong
                if (index < firstDayOffset) {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .height(56.dp)
                    ) {}
                    return@itemsIndexed
                }

                // Hitung tanggal hari ini
                val dayNumber = index - firstDayOffset + 1
                val date = monthFocused.withDayOfMonth(dayNumber)

                val isSelected = selectedDate == date

                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectDate(date) }
                        .background(
                            if (isSelected) Color(0xFFEFF6FF) else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dayNumber.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color(0xFF3D8AFF) else Color.Black
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .size(width = 18.dp, height = 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isSelected) Color(0xFF3D8AFF) else Color(0xFFE4E4E4)
                                )
                        )
                    }
                }
            }
        }
    }
}
