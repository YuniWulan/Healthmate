package com.example.healthmateapp.navigation

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.healthmateapp.screens.AddMedicationScreen
import com.example.healthmateapp.screens.HomeScreen
import com.example.healthmateapp.screens.MedicationScheduleScreen
import com.example.healthmateapp.screens.Reminder
import com.example.healthmateapp.viewmodel.MedicationViewModel
import java.time.LocalDate
import androidx.compose.ui.graphics.Color

/**
 * Navigation graph for medication-related screens.
 *
 * This example shows how to integrate MedicationViewModel with your screens.
 * Add these composable functions to your main NavHost.
 */

fun NavGraphBuilder.medicationNavigation(
    navController: NavController,
    medicationViewModel: MedicationViewModel
) {
    // Home Screen
    composable("home") {
        val medications by medicationViewModel.medications.collectAsState()
        val isLoading by medicationViewModel.isLoading.collectAsState()
        val error by medicationViewModel.error.collectAsState()
        val success by medicationViewModel.operationSuccess.collectAsState()

        val context = LocalContext.current

        // Show error toast
        LaunchedEffect(error) {
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                medicationViewModel.clearError()
            }
        }

        // Show success toast
        LaunchedEffect(success) {
            success?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                medicationViewModel.clearSuccess()
            }
        }

        // Get today's medications and convert to Reminder format for HomeScreen
        val today = LocalDate.now()
        val todaysMedications = medications[today] ?: emptyList()
        val reminders = todaysMedications.mapIndexed { index, med ->
            Reminder(
                id = index,
                date = today.toString(),
                time = med.time,
                title = med.name,
                iconColor = Color(0xFF0A84FF), // You can customize colors
                dosage = med.dosage,
                note = med.note
            )
        }

        HomeScreen(
            reminders = reminders,
            onNotificationClick = { navController.navigate("notifications") },
            onProfileClick = { navController.navigate("profile") },
            onReminderClick = { reminder ->
                navController.navigate("reminder")
            },
            onRecordConsumptionClick = { navController.navigate("record_consumption") },
            onBottomNavClick = { route -> navController.navigate(route) },
            onInputBloodPressure = { navController.navigate("input_blood_pressure") },
            onInputBloodGlucose = { navController.navigate("input_blood_glucose") },
            onInputCholesterol = { navController.navigate("input_cholesterol") },
            onInputBodyComposition = { navController.navigate("input_body_composition") },
            onConfirmTaken = { reminder ->
                // Find the medication and mark it as taken
                val medication = todaysMedications.find { it.name == reminder.title }
                medication?.let {
                    medicationViewModel.toggleMedicationTaken(
                        date = today,
                        medicationId = it.id,
                        taken = true
                    )
                }
            }
        )
    }

    // Medication Schedule Screen
    composable("reminder") {
        val medications by medicationViewModel.medications.collectAsState()
        val isLoading by medicationViewModel.isLoading.collectAsState()
        val error by medicationViewModel.error.collectAsState()
        val success by medicationViewModel.operationSuccess.collectAsState()

        val context = LocalContext.current

        // Show error toast
        LaunchedEffect(error) {
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                medicationViewModel.clearError()
            }
        }

        // Show success toast
        LaunchedEffect(success) {
            success?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                medicationViewModel.clearSuccess()
            }
        }

        MedicationScheduleScreen(
            navController = navController,
            medications = medications,
            onToggleTaken = { date, medicationId, taken ->
                medicationViewModel.toggleMedicationTaken(
                    date = date,
                    medicationId = medicationId,
                    taken = taken
                )
            },
            onEditMedication = { date, medicationId ->
                // Navigate to edit screen (you can implement this)
                navController.navigate("edit_medication/$medicationId")
            },
            onDeleteMedication = { date, medicationId ->
                medicationViewModel.deleteMedication(date, medicationId)
            },
            onAddMedicationClick = {
                navController.navigate("add_medication")
            }
        )
    }

    // Add Medication Screen
    composable("add_medication") {
        val context = LocalContext.current

        val error by medicationViewModel.error.collectAsState()
        val success by medicationViewModel.operationSuccess.collectAsState()

        // Show error toast
        LaunchedEffect(error) {
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                medicationViewModel.clearError()
            }
        }

        // Show success toast and navigate back
        LaunchedEffect(success) {
            success?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                medicationViewModel.clearSuccess()
                navController.popBackStack()
            }
        }

        AddMedicationScreen(
            onBackClick = {
                navController.popBackStack()
            },
            onSaveClick = { name, dosage, frequency, startDate, endDate, beforeMeal, time, customInstruction ->
                // Combine note and custom instruction
                val note = if (beforeMeal) "Before Meal" else "After Meal"

                medicationViewModel.addMedication(
                    name = name,
                    dosage = dosage,
                    note = note,
                    time = time,
                    frequency = frequency,
                    startDate = startDate,
                    endDate = endDate,
                    beforeMeal = beforeMeal
                )
            }
        )
    }
}

/**
 * Example usage in MainActivity or main navigation setup:
 *
 * @Composable
 * fun MainNavigation() {
 *     val navController = rememberNavController()
 *     val medicationViewModel: MedicationViewModel = viewModel()
 *
 *     NavHost(
 *         navController = navController,
 *         startDestination = "home"
 *     ) {
 *         medicationNavigation(navController, medicationViewModel)
 *
 *         // Add other navigation routes here
 *     }
 * }
 */