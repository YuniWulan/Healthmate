package com.example.healthmateapp.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmateapp.screens.Medication
import com.example.healthmateapp.screens.TakenDetails
import com.example.healthmateapp.screens.alarm.MedicationAlarmScheduler
import com.example.healthmateapp.screens.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MedicationViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private val repository = MedicationRepository()

    private val _medications = MutableStateFlow<Map<LocalDate, List<Medication>>>(emptyMap())
    val medications: StateFlow<Map<LocalDate, List<Medication>>> = _medications

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _operationSuccess = MutableStateFlow<String?>(null)
    val operationSuccess: StateFlow<String?> = _operationSuccess

    companion object {
        private const val TAG = "MedicationViewModel"
    }

    init {
        loadMedicationsFromFirebase()
    }

    /**
     * Load all medications from Firebase
     */
    private fun loadMedicationsFromFirebase() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllMedications()
                .catch { e ->
                    Log.e(TAG, "Error loading medications", e)
                    _error.value = "Failed to load medications: ${e.message}"
                    _isLoading.value = false
                }
                .collect { medicationsMap ->
                    _medications.value = medicationsMap
                    _isLoading.value = false
                    Log.d(TAG, "Medications loaded: ${medicationsMap.size} dates")
                }
        }
    }

    /**
     * Add a new medication and save to Firebase
     */
    fun addMedication(
        name: String,
        dosage: String,
        note: String,
        time: String,
        frequency: String,
        startDate: LocalDate,
        endDate: LocalDate,
        beforeMeal: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = repository.addMedication(
                name = name,
                dosage = dosage,
                note = note,
                time = time,
                frequency = frequency,
                startDate = startDate,
                endDate = endDate,
                beforeMeal = beforeMeal
            )

            result.fold(
                onSuccess = { medicationId ->
                    Log.d(TAG, "Medication added successfully: $medicationId")
                    _operationSuccess.value = "Medication added successfully"

                    // Schedule alarms
                    scheduleAlarmsForMedication(
                        medicationId = medicationId,
                        name = name,
                        dosage = dosage,
                        note = note,
                        time = time,
                        startDate = startDate,
                        endDate = endDate
                    )
                },
                onFailure = { e ->
                    Log.e(TAG, "Error adding medication", e)
                    _error.value = "Failed to add medication: ${e.message}"
                }
            )

            _isLoading.value = false
        }
    }

    /**
     * Schedule alarms for a medication
     */
    private fun scheduleAlarmsForMedication(
        medicationId: String,
        name: String,
        dosage: String,
        note: String,
        time: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        val localTime = try {
            // Try 12-hour format first
            val formatter12Hour = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
            LocalTime.parse(time, formatter12Hour)
        } catch (e1: Exception) {
            try {
                // Try 24-hour format
                val formatter24Hour = DateTimeFormatter.ofPattern("HH:mm")
                LocalTime.parse(time, formatter24Hour)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to parse time '$time'. Error: ${e2.message}")
                LocalTime.of(8, 0) // Default to 8:00 AM
            }
        }

        Log.d(TAG, "✅ Scheduling alarm for '$name' at $localTime (parsed from '$time') on dates $startDate to $endDate")

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

        Log.d(TAG, "✅ Alarm scheduling completed for '$name'")
    }

    /**
     * Toggle medication taken status and save to Firebase
     */
    fun toggleMedicationTaken(
        date: LocalDate,
        medicationId: String,
        taken: Boolean,
        takenDetails: TakenDetails? = null
    ) {
        viewModelScope.launch {
            val result = repository.updateMedicationTaken(
                medicationId = medicationId,
                date = date,
                taken = taken,
                takenDetails = takenDetails
            )

            result.fold(
                onSuccess = {
                    Log.d(TAG, "Medication taken status updated successfully")
                    _operationSuccess.value = if (taken) "Medication marked as taken" else "Medication marked as not taken"
                },
                onFailure = { e ->
                    Log.e(TAG, "Error updating medication taken status", e)
                    _error.value = "Failed to update medication: ${e.message}"
                }
            )
        }
    }

    /**
     * Delete a medication and remove from Firebase
     */
    fun deleteMedication(date: LocalDate, medicationId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            // Get medication details before deleting for alarm cancellation
            val medication = _medications.value[date]?.find {
                it.id == medicationId || it.id.startsWith(medicationId)
            }

            val result = repository.deleteMedication(medicationId)

            result.fold(
                onSuccess = {
                    Log.d(TAG, "Medication deleted successfully")
                    _operationSuccess.value = "Medication deleted successfully"

                    // Cancel alarms
                    medication?.let {
                        val localTime = try {
                            val formatter12Hour = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
                            LocalTime.parse(it.time, formatter12Hour)
                        } catch (e1: Exception) {
                            try {
                                val formatter24Hour = DateTimeFormatter.ofPattern("HH:mm")
                                LocalTime.parse(it.time, formatter24Hour)
                            } catch (e2: Exception) {
                                LocalTime.of(8, 0)
                            }
                        }

                        MedicationAlarmScheduler.cancelAlarm(
                            context = context,
                            medicationId = medicationId,
                            date = date,
                            time = localTime
                        )
                    }
                },
                onFailure = { e ->
                    Log.e(TAG, "Error deleting medication", e)
                    _error.value = "Failed to delete medication: ${e.message}"
                }
            )

            _isLoading.value = false
        }
    }

    /**
     * Update medication details
     */
    fun updateMedication(
        medicationId: String,
        name: String,
        dosage: String,
        note: String,
        time: String,
        frequency: String,
        startDate: LocalDate,
        endDate: LocalDate,
        beforeMeal: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = repository.updateMedication(
                medicationId = medicationId,
                name = name,
                dosage = dosage,
                note = note,
                time = time,
                frequency = frequency,
                startDate = startDate,
                endDate = endDate,
                beforeMeal = beforeMeal
            )

            result.fold(
                onSuccess = {
                    Log.d(TAG, "Medication updated successfully")
                    _operationSuccess.value = "Medication updated successfully"

                    // Reschedule alarms with new details
                    scheduleAlarmsForMedication(
                        medicationId = medicationId,
                        name = name,
                        dosage = dosage,
                        note = note,
                        time = time,
                        startDate = startDate,
                        endDate = endDate
                    )
                },
                onFailure = { e ->
                    Log.e(TAG, "Error updating medication", e)
                    _error.value = "Failed to update medication: ${e.message}"
                }
            )

            _isLoading.value = false
        }
    }

    /**
     * Get medications for a specific date
     */
    fun getMedicationsForDate(date: LocalDate): List<Medication> {
        return _medications.value[date] ?: emptyList()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clear success message
     */
    fun clearSuccess() {
        _operationSuccess.value = null
    }

    /**
     * Refresh medications from Firebase
     */
    fun refreshMedications() {
        loadMedicationsFromFirebase()
    }
}