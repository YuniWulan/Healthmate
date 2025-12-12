package com.example.healthmateapp.screens.repository

import android.util.Log
import com.example.healthmateapp.screens.Medication
import com.example.healthmateapp.screens.TakenDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/**
 * Repository for managing medication data in Firebase Firestore.
 *
 * Firebase Structure:
 * /users/{userId}/patients/{patientId}/reminders/{reminderId}
 *
 * ✅ Matches existing Firestore structure (lowercase plural)
 */
class MedicationRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "MedicationRepository"
        // ✅ Using your EXISTING structure
        private const val COLLECTION_USERS = "users"  // lowercase plural
        private const val COLLECTION_PATIENTS = "patients"  // lowercase plural
        private const val COLLECTION_REMINDERS = "reminders"  // Keep as reminders
    }

    /**
     * Get the current user ID from Firebase Auth
     */
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Get patient ID for the current user
     */
    private fun getPatientId(): String? {
        return getCurrentUserId()
    }

    /**
     * Add a new medication reminder to Firebase
     */
    suspend fun addMedication(
        name: String,
        dosage: String,
        note: String,
        time: String,
        frequency: String,
        startDate: LocalDate,
        endDate: LocalDate,
        beforeMeal: Boolean
    ): Result<String> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            val patientId = getPatientId() ?: return Result.failure(Exception("Patient ID not found"))

            // Generate unique reminder ID
            val reminderId = db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_PATIENTS)
                .document(patientId)
                .collection(COLLECTION_REMINDERS)
                .document()
                .id

            val medicationData = hashMapOf(
                "id" to reminderId,
                "name" to name,
                "dosage" to dosage,
                "note" to note,
                "time" to time,
                "frequency" to frequency,
                "startDate" to startDate.toString(),
                "endDate" to endDate.toString(),
                "beforeMeal" to beforeMeal,
                "taken" to false,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            Log.d(TAG, "💾 Saving to: users/$userId/patients/$patientId/reminders/$reminderId")

            db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_PATIENTS)
                .document(patientId)
                .collection(COLLECTION_REMINDERS)
                .document(reminderId)
                .set(medicationData)
                .await()

            Log.d(TAG, "✅ Medication added successfully: $reminderId")
            Result.success(reminderId)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error adding medication", e)
            Result.failure(e)
        }
    }

    /**
     * Get all medications across all dates as a Flow with real-time updates
     */
    fun getAllMedications(): Flow<Map<LocalDate, List<Medication>>> = callbackFlow {
        val userId = getCurrentUserId()
        val patientId = getPatientId()

        if (userId == null || patientId == null) {
            Log.e(TAG, "❌ User not authenticated")
            trySend(emptyMap())
            close()
            return@callbackFlow
        }

        Log.d(TAG, "📡 Setting up listener: users/$userId/patients/$patientId/reminders")

        val listenerRegistration = db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_PATIENTS)
            .document(patientId)
            .collection(COLLECTION_REMINDERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Listener error", error)
                    trySend(emptyMap())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d(TAG, "📡 Received ${snapshot.documents.size} medications")

                    val medicationsMap = mutableMapOf<LocalDate, MutableList<Medication>>()

                    snapshot.documents.forEach { doc ->
                        try {
                            val startDateStr = doc.getString("startDate") ?: return@forEach
                            val endDateStr = doc.getString("endDate") ?: return@forEach

                            val startDate = LocalDate.parse(startDateStr)
                            val endDate = LocalDate.parse(endDateStr)

                            // Get taken history
                            val takenHistory = doc.get("takenHistory") as? Map<*, *> ?: emptyMap<String, Any>()

                            // Generate medication entries for each date in range
                            var currentDate = startDate
                            while (!currentDate.isAfter(endDate)) {
                                val dateString = currentDate.toString()
                                val takenOnDate = takenHistory[dateString] as? Boolean ?: false

                                val takenDetailsMap = takenHistory["${dateString}_details"] as? Map<*, *>
                                val takenDetails = if (takenDetailsMap != null) {
                                    TakenDetails(
                                        day = takenDetailsMap["day"] as? String ?: "",
                                        time = takenDetailsMap["time"] as? String ?: "",
                                        date = takenDetailsMap["date"] as? String ?: "",
                                        notes = takenDetailsMap["notes"] as? String ?: "",
                                        photoUri = null
                                    )
                                } else null

                                val medication = Medication(
                                    id = "${doc.id}_${dateString}",
                                    name = doc.getString("name") ?: "",
                                    dosage = doc.getString("dosage") ?: "",
                                    note = doc.getString("note") ?: "",
                                    time = doc.getString("time") ?: "",
                                    taken = takenOnDate,
                                    takenDetails = takenDetails
                                )

                                if (!medicationsMap.containsKey(currentDate)) {
                                    medicationsMap[currentDate] = mutableListOf()
                                }
                                medicationsMap[currentDate]?.add(medication)

                                currentDate = currentDate.plusDays(1)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Error parsing doc: ${doc.id}", e)
                        }
                    }

                    // Sort by time
                    val sortedMap = medicationsMap.mapValues { (_, meds) ->
                        meds.sortedBy { it.time }
                    }

                    Log.d(TAG, "✅ Processed ${sortedMap.size} dates with medications")
                    trySend(sortedMap)
                } else {
                    Log.d(TAG, "📭 No medications found")
                    trySend(emptyMap())
                }
            }

        awaitClose {
            Log.d(TAG, "🔌 Removing listener")
            listenerRegistration.remove()
        }
    }

    /**
     * Get medications for a specific date
     */
    fun getMedicationsForDate(date: LocalDate): Flow<List<Medication>> = callbackFlow {
        val userId = getCurrentUserId()
        val patientId = getPatientId()

        if (userId == null || patientId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val dateString = date.toString()

        val listenerRegistration = db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_PATIENTS)
            .document(patientId)
            .collection(COLLECTION_REMINDERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error for date $date", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val medications = snapshot.documents.mapNotNull { doc ->
                        try {
                            val startDateStr = doc.getString("startDate") ?: return@mapNotNull null
                            val endDateStr = doc.getString("endDate") ?: return@mapNotNull null

                            val startDate = LocalDate.parse(startDateStr)
                            val endDate = LocalDate.parse(endDateStr)

                            if (date.isBefore(startDate) || date.isAfter(endDate)) {
                                return@mapNotNull null
                            }

                            val takenHistory = doc.get("takenHistory") as? Map<*, *>
                            val takenOnDate = takenHistory?.get(dateString) as? Boolean ?: false

                            val takenDetailsMap = takenHistory?.get("${dateString}_details") as? Map<*, *>
                            val takenDetails = if (takenDetailsMap != null) {
                                TakenDetails(
                                    day = takenDetailsMap["day"] as? String ?: "",
                                    time = takenDetailsMap["time"] as? String ?: "",
                                    date = takenDetailsMap["date"] as? String ?: "",
                                    notes = takenDetailsMap["notes"] as? String ?: "",
                                    photoUri = null
                                )
                            } else null

                            Medication(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                dosage = doc.getString("dosage") ?: "",
                                note = doc.getString("note") ?: "",
                                time = doc.getString("time") ?: "",
                                taken = takenOnDate,
                                takenDetails = takenDetails
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing doc", e)
                            null
                        }
                    }.sortedBy { it.time }

                    trySend(medications)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Update medication taken status
     */
    suspend fun updateMedicationTaken(
        medicationId: String,
        date: LocalDate,
        taken: Boolean,
        takenDetails: TakenDetails? = null
    ): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            val patientId = getPatientId() ?: return Result.failure(Exception("Patient ID not found"))

            val baseMedicationId = medicationId.substringBefore("_${date}")
            val dateString = date.toString()

            val updates = hashMapOf<String, Any>(
                "takenHistory.$dateString" to taken,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            if (takenDetails != null) {
                updates["takenHistory.${dateString}_details"] = hashMapOf(
                    "day" to takenDetails.day,
                    "time" to takenDetails.time,
                    "date" to takenDetails.date,
                    "notes" to takenDetails.notes
                )
            }

            Log.d(TAG, "💾 Updating: users/$userId/patients/$patientId/reminders/$baseMedicationId")

            db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_PATIENTS)
                .document(patientId)
                .collection(COLLECTION_REMINDERS)
                .document(baseMedicationId)
                .update(updates)
                .await()

            Log.d(TAG, "✅ Updated taken status for $dateString")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating taken status", e)
            Result.failure(e)
        }
    }

    /**
     * Delete medication
     */
    suspend fun deleteMedication(medicationId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            val patientId = getPatientId() ?: return Result.failure(Exception("Patient ID not found"))

            val baseMedicationId = medicationId.substringBefore("_")

            Log.d(TAG, "🗑️ Deleting: users/$userId/patients/$patientId/reminders/$baseMedicationId")

            db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_PATIENTS)
                .document(patientId)
                .collection(COLLECTION_REMINDERS)
                .document(baseMedicationId)
                .delete()
                .await()

            Log.d(TAG, "✅ Deleted successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting", e)
            Result.failure(e)
        }
    }

    /**
     * Update medication details
     */
    suspend fun updateMedication(
        medicationId: String,
        name: String,
        dosage: String,
        note: String,
        time: String,
        frequency: String,
        startDate: LocalDate,
        endDate: LocalDate,
        beforeMeal: Boolean
    ): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            val patientId = getPatientId() ?: return Result.failure(Exception("Patient ID not found"))

            val updates = hashMapOf<String, Any>(
                "name" to name,
                "dosage" to dosage,
                "note" to note,
                "time" to time,
                "frequency" to frequency,
                "startDate" to startDate.toString(),
                "endDate" to endDate.toString(),
                "beforeMeal" to beforeMeal,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            Log.d(TAG, "💾 Updating: users/$userId/patients/$patientId/reminders/$medicationId")

            db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_PATIENTS)
                .document(patientId)
                .collection(COLLECTION_REMINDERS)
                .document(medicationId)
                .update(updates)
                .await()

            Log.d(TAG, "✅ Updated successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating", e)
            Result.failure(e)
        }
    }
}