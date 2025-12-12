package com.example.healthmateapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmateapp.screens.Medication
import com.example.healthmateapp.screens.alarm.MedicationAlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MedicationViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext

    private val _medications = MutableStateFlow<Map<LocalDate, List<Medication>>>(emptyMap())
    val medications: StateFlow<Map<LocalDate, List<Medication>>> = _medications

    init {
        // Load sample data or from database
        loadMedications()
    }

    private fun loadMedications() {
        // This is sample data - in production, load from database
        viewModelScope.launch {
            val sampleMedications = mutableMapOf<LocalDate, List<Medication>>()

            // Example medications for today
            val today = LocalDate.now()
            sampleMedications[today] = listOf(
                Medication(
                    id = "med_1",
                    name = "Aspirin",
                    dosage = "100mg",
                    note = "After Eating",
                    time = "08:00",
                    taken = false
                ),
                Medication(
                    id = "med_2",
                    name = "Metformin",
                    dosage = "500mg",
                    note = "Before Eating",
                    time = "12:00",
                    taken = false
                )
            )

            _medications.value = sampleMedications
        }
    }

    fun addMedication(
        name: String,
        dosage: String,
        note: String,
        time: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        viewModelScope.launch {
            val medicationId = "med_${System.currentTimeMillis()}"

            // Create medication for each date in the range
            val currentMedications = _medications.value.toMutableMap()
            var currentDate = startDate

            while (!currentDate.isAfter(endDate)) {
                val medication = Medication(
                    id = "${medicationId}_${currentDate}",
                    name = name,
                    dosage = dosage,
                    note = note,
                    time = time,
                    taken = false
                )

                val existingMeds = currentMedications[currentDate]?.toMutableList() ?: mutableListOf()
                existingMeds.add(medication)
                currentMedications[currentDate] = existingMeds

                currentDate = currentDate.plusDays(1)
            }

            _medications.value = currentMedications

            // Schedule alarms for this medication
            scheduleAlarmsForMedication(
                medicationId = medicationId,
                name = name,
                dosage = dosage,
                note = note,
                time = time,
                startDate = startDate,
                endDate = endDate
            )
        }
    }

    private fun scheduleAlarmsForMedication(
        medicationId: String,
        name: String,
        dosage: String,
        note: String,
        time: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        // Parse time string (e.g., "08:00" or "12:30")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val localTime = try {
            LocalTime.parse(time, timeFormatter)
        } catch (e: Exception) {
            LocalTime.of(8, 0) // Default to 8:00 AM if parsing fails
        }

        // Schedule alarms for the date range
        MedicationAlarmScheduler.scheduleAlarmsForDateRange(
            context = context,
            medicationId = medicationId,
            medicationName = name,
            medicationDosage = dosage,
            medicationNote = note,
            startDate = startDate,
            endDate = endDate,
            time = localTime
        )
    }

    fun toggleMedicationTaken(date: LocalDate, medicationId: String, taken: Boolean) {
        viewModelScope.launch {
            val currentMedications = _medications.value.toMutableMap()
            val medsForDate = currentMedications[date]?.toMutableList() ?: return@launch

            val index = medsForDate.indexOfFirst { it.id == medicationId }
            if (index != -1) {
                medsForDate[index] = medsForDate[index].copy(taken = taken)
                currentMedications[date] = medsForDate
                _medications.value = currentMedications
            }
        }
    }

    fun deleteMedication(date: LocalDate, medicationId: String) {
        viewModelScope.launch {
            val currentMedications = _medications.value.toMutableMap()
            val medsForDate = currentMedications[date]?.toMutableList() ?: return@launch

            val medication = medsForDate.find { it.id == medicationId }

            medsForDate.removeAll { it.id == medicationId }

            if (medsForDate.isEmpty()) {
                currentMedications.remove(date)
            } else {
                currentMedications[date] = medsForDate
            }

            _medications.value = currentMedications

            // Cancel the alarm for this medication
            medication?.let {
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val localTime = try {
                    LocalTime.parse(it.time, timeFormatter)
                } catch (e: Exception) {
                    LocalTime.of(8, 0)
                }

                MedicationAlarmScheduler.cancelAlarm(
                    context = context,
                    medicationId = medicationId,
                    date = date,
                    time = localTime
                )
            }
        }
    }

    fun getMedicationsForDate(date: LocalDate): List<Medication> {
        return _medications.value[date] ?: emptyList()
    }
}