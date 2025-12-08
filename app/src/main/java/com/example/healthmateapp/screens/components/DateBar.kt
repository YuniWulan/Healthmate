package com.example.healthmateapp.screens.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.time.LocalDate
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.ui.unit.dp
import com.example.healthmateapp.ui.theme.BlueMain

@Composable
fun DateBar(
    currentDate: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val startDate = currentDate.minusDays(3)
    val dates = List(7) { startDate.plusDays(it.toLong()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        dates.forEach { date ->
            DateItem(
                date = date,
                isSelected = date == selectedDate,
                onSelectDate = { onDateSelected(it) }
            )
        }
    }
}
