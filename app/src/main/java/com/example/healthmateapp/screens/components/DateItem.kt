package com.example.healthmateapp.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import com.example.healthmateapp.ui.theme.BlueMain
import androidx.compose.foundation.border


@Composable
fun DateItem(
    date: LocalDate,
    isSelected: Boolean,
    onSelectDate: (LocalDate) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) BlueMain else Color.White)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelectDate(date) }
            .padding(horizontal = 18.dp, vertical = 12.dp) // 🔥 kotak BESAR
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // Hari (Sen, Sel, Rab)
            Text(
                text = date.dayOfWeek
                    .getDisplayName(TextStyle.SHORT, Locale("id"))
                    .replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                color = if (isSelected) Color.White else Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Angka tanggal
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Indikator kecil kotak
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.White else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

