package com.example.healthmateapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data classes for health metrics
data class BloodPressureData(
    val systolic: String = "",
    val diastolic: String = "",
    val heartRate: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class BloodGlucoseData(
    val glucose: String = "",
    val testType: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class CholesterolData(
    val total: String = "",
    val ldl: String = "",
    val hdl: String = "",
    val triglycerides: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class BodyCompositionData(
    val bodyFat: String = "",
    val muscleMass: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class HealthMetrics(
    val bloodPressure: BloodPressureData? = null,
    val bloodGlucose: BloodGlucoseData? = null,
    val cholesterol: CholesterolData? = null,
    val bodyComposition: BodyCompositionData? = null
)

sealed class HealthMetricsState {
    object Idle : HealthMetricsState()
    object Loading : HealthMetricsState()
    data class Success(val metrics: HealthMetrics) : HealthMetricsState()
    data class Error(val message: String) : HealthMetricsState()
}

class HealthMetricsViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _healthMetricsState = MutableStateFlow<HealthMetricsState>(HealthMetricsState.Idle)
    val healthMetricsState: StateFlow<HealthMetricsState> = _healthMetricsState

    private val _currentMetrics = MutableStateFlow(HealthMetrics())
    val currentMetrics: StateFlow<HealthMetrics> = _currentMetrics

    companion object {
        private const val TAG = "HealthMetricsVM"
    }

    init {
        loadHealthMetrics()
    }

    // Load health metrics from Firebase
    // Structure: User/{uid}/patient/{uid}/healthMetrics/latest
    fun loadHealthMetrics() {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            Log.e(TAG, "No user logged in!")
            return
        }

        Log.d(TAG, "Loading health metrics for uid: $uid")

        viewModelScope.launch {
            try {
                _healthMetricsState.value = HealthMetricsState.Loading

                // Path: User/{uid}/patient/{uid}/healthMetrics/latest
                val docRef = firestore.collection("User")
                    .document(uid)
                    .collection("patient")
                    .document(uid)
                    .collection("healthMetrics")
                    .document("latest")

                Log.d(TAG, "Fetching document from: User/$uid/patient/$uid/healthMetrics/latest")

                val snapshot = docRef.get().await()

                if (snapshot.exists()) {
                    Log.d(TAG, "Document exists! Data: ${snapshot.data}")

                    val bloodPressure = snapshot.get("bloodPressure") as? Map<*, *>
                    val bloodGlucose = snapshot.get("bloodGlucose") as? Map<*, *>
                    val cholesterol = snapshot.get("cholesterol") as? Map<*, *>
                    val bodyComposition = snapshot.get("bodyComposition") as? Map<*, *>

                    val metrics = HealthMetrics(
                        bloodPressure = bloodPressure?.let {
                            BloodPressureData(
                                systolic = it["systolic"] as? String ?: "",
                                diastolic = it["diastolic"] as? String ?: "",
                                heartRate = it["heartRate"] as? String ?: "",
                                note = it["note"] as? String ?: "",
                                timestamp = it["timestamp"] as? Long ?: System.currentTimeMillis()
                            )
                        },
                        bloodGlucose = bloodGlucose?.let {
                            BloodGlucoseData(
                                glucose = it["glucose"] as? String ?: "",
                                testType = it["testType"] as? String ?: "",
                                note = it["note"] as? String ?: "",
                                timestamp = it["timestamp"] as? Long ?: System.currentTimeMillis()
                            )
                        },
                        cholesterol = cholesterol?.let {
                            CholesterolData(
                                total = it["total"] as? String ?: "",
                                ldl = it["ldl"] as? String ?: "",
                                hdl = it["hdl"] as? String ?: "",
                                triglycerides = it["triglycerides"] as? String ?: "",
                                note = it["note"] as? String ?: "",
                                timestamp = it["timestamp"] as? Long ?: System.currentTimeMillis()
                            )
                        },
                        bodyComposition = bodyComposition?.let {
                            BodyCompositionData(
                                bodyFat = it["bodyFat"] as? String ?: "",
                                muscleMass = it["muscleMass"] as? String ?: "",
                                note = it["note"] as? String ?: "",
                                timestamp = it["timestamp"] as? Long ?: System.currentTimeMillis()
                            )
                        }
                    )

                    _currentMetrics.value = metrics
                    _healthMetricsState.value = HealthMetricsState.Success(metrics)
                    Log.d(TAG, "Successfully loaded metrics: $metrics")
                } else {
                    Log.d(TAG, "Document doesn't exist yet. Creating empty metrics.")
                    _currentMetrics.value = HealthMetrics()
                    _healthMetricsState.value = HealthMetricsState.Success(HealthMetrics())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading health metrics: ${e.message}", e)
                _healthMetricsState.value = HealthMetricsState.Error(
                    e.message ?: "Failed to load health metrics"
                )
            }
        }
    }

    // Save Blood Pressure
    fun saveBloodPressure(systolic: String, diastolic: String, heartRate: String, note: String) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            Log.e(TAG, "No user logged in! Cannot save blood pressure.")
            _healthMetricsState.value = HealthMetricsState.Error("User not logged in")
            return
        }

        Log.d(TAG, "Saving blood pressure for uid: $uid")
        Log.d(TAG, "Values - Systolic: $systolic, Diastolic: $diastolic, Heart Rate: $heartRate")

        viewModelScope.launch {
            try {
                _healthMetricsState.value = HealthMetricsState.Loading

                val bloodPressureData = BloodPressureData(
                    systolic = systolic,
                    diastolic = diastolic,
                    heartRate = heartRate,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )

                // Path: User/{uid}/patient/{uid}/healthMetrics/latest
                val docRef = firestore.collection("User")
                    .document(uid)
                    .collection("patient")
                    .document(uid)
                    .collection("healthMetrics")
                    .document("latest")

                Log.d(TAG, "Saving to: User/$uid/patient/$uid/healthMetrics/latest")
                Log.d(TAG, "Data: $bloodPressureData")

                docRef.set(
                    mapOf("bloodPressure" to bloodPressureData),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()

                Log.d(TAG, "✅ Successfully saved to 'latest' document")

                // Also save to history
                val historyRef = firestore.collection("User")
                    .document(uid)
                    .collection("patient")
                    .document(uid)
                    .collection("healthMetrics")
                    .document("history")
                    .collection("bloodPressure")
                    .add(bloodPressureData)
                    .await()

                Log.d(TAG, "✅ Successfully saved to history: ${historyRef.id}")

                _currentMetrics.value = _currentMetrics.value.copy(
                    bloodPressure = bloodPressureData
                )
                _healthMetricsState.value = HealthMetricsState.Success(_currentMetrics.value)

                Log.d(TAG, "✅ Blood pressure saved successfully!")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error saving blood pressure: ${e.message}", e)
                _healthMetricsState.value = HealthMetricsState.Error(
                    e.message ?: "Failed to save blood pressure"
                )
            }
        }
    }

    // Save Blood Glucose
    fun saveBloodGlucose(glucose: String, testType: String, note: String) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            Log.e(TAG, "No user logged in! Cannot save blood glucose.")
            _healthMetricsState.value = HealthMetricsState.Error("User not logged in")
            return
        }

        Log.d(TAG, "Saving blood glucose for uid: $uid")
        Log.d(TAG, "Values - Glucose: $glucose, Test Type: $testType")

        viewModelScope.launch {
            try {
                _healthMetricsState.value = HealthMetricsState.Loading

                val bloodGlucoseData = BloodGlucoseData(
                    glucose = glucose,
                    testType = testType,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )

                val docRef = firestore.collection("User")
                    .document(uid)
                    .collection("patient")
                    .document(uid)
                    .collection("healthMetrics")
                    .document("latest")

                Log.d(TAG, "Saving to: User/$uid/patient/$uid/healthMetrics/latest")

                docRef.set(
                    mapOf("bloodGlucose" to bloodGlucoseData),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()

                Log.d(TAG, "✅ Successfully saved to 'latest' document")

                val historyRef = firestore.collection("User")
                    .document(uid)
                    .collection("patient")
                    .document(uid)
                    .collection("healthMetrics")
                    .document("history")
                    .collection("bloodGlucose")
                    .add(bloodGlucoseData)
                    .await()

                Log.d(TAG, "✅ Successfully saved to history: ${historyRef.id}")

                _currentMetrics.value = _currentMetrics.value.copy(
                    bloodGlucose = bloodGlucoseData
                )
                _healthMetricsState.value = HealthMetricsState.Success(_currentMetrics.value)

                Log.d(TAG, "✅ Blood glucose saved successfully!")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error saving blood glucose: ${e.message}", e)
                _healthMetricsState.value = HealthMetricsState.Error(
                    e.message ?: "Failed to save blood glucose"
                )
            }
        }
    }

    // Save Cholesterol
    fun saveCholesterol(total: String, ldl: String, hdl: String, triglycerides: String, note: String) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            Log.e(TAG, "No user logged in! Cannot save cholesterol.")
            _healthMetricsState.value = HealthMetricsState.Error("User not logged in")
            return
        }

        Log.d(TAG, "Saving cholesterol for uid: $uid")
        Log.d(TAG, "Values - Total: $total, LDL: $ldl, HDL: $hdl, Triglycerides: $triglycerides")

        viewModelScope.launch {
            try {
                _healthMetricsState.value = HealthMetricsState.Loading

                val cholesterolData = CholesterolData(
                    total = total,
                    ldl = ldl,
                    hdl = hdl,
                    triglycerides = triglycerides,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )

                val docRef = firestore.collection("User")
                    .document(uid)
                    .collection("patient")
                    .document(uid)
                    .collection("healthMetrics")
                    .document("latest")

                Log.d(TAG, "Saving to: User/$uid/patient/$uid/healthMetrics/latest")

                docRef.set(
                    mapOf("cholesterol" to cholesterolData),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()

                Log.d(TAG, "✅ Successfully saved to 'latest' document")

                val historyRef = firestore.collection("User")
                    .document(uid)
                    .collection("patient")
                    .document(uid)
                    .collection("healthMetrics")
                    .document("history")
                    .collection("cholesterol")
                    .add(cholesterolData)
                    .await()

                Log.d(TAG, "✅ Successfully saved to history: ${historyRef.id}")

                _currentMetrics.value = _currentMetrics.value.copy(
                    cholesterol = cholesterolData
                )
                _healthMetricsState.value = HealthMetricsState.Success(_currentMetrics.value)

                Log.d(TAG, "✅ Cholesterol saved successfully!")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error saving cholesterol: ${e.message}", e)
                _healthMetricsState.value = HealthMetricsState.Error(
                    e.message ?: "Failed to save cholesterol"
                )
            }
        }
    }

    // Save Body Composition
    fun saveBodyComposition(bodyFat: String, muscleMass: String, note: String) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            Log.e(TAG, "No user logged in! Cannot save body composition.")
            _healthMetricsState.value = HealthMetricsState.Error("User not logged in")
            return
        }

        Log.d(TAG, "Saving body composition for uid: $uid")
        Log.d(TAG, "Values - Body Fat: $bodyFat, Muscle Mass: $muscleMass")

        viewModelScope.launch {
            try {
                _healthMetricsState.value = HealthMetricsState.Loading

                val bodyCompositionData = BodyCompositionData(
                    bodyFat = bodyFat,
                    muscleMass = muscleMass,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )

                val docRef = firestore.collection("User")
                    .document(uid)
                    .collection("patient")
                    .document(uid)
                    .collection("healthMetrics")
                    .document("latest")

                Log.d(TAG, "Saving to: User/$uid/patient/$uid/healthMetrics/latest")

                docRef.set(
                    mapOf("bodyComposition" to bodyCompositionData),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()

                Log.d(TAG, "✅ Successfully saved to 'latest' document")

                val historyRef = firestore.collection("User")
                    .document(uid)
                    .collection("patient")
                    .document(uid)
                    .collection("healthMetrics")
                    .document("history")
                    .collection("bodyComposition")
                    .add(bodyCompositionData)
                    .await()

                Log.d(TAG, "✅ Successfully saved to history: ${historyRef.id}")

                _currentMetrics.value = _currentMetrics.value.copy(
                    bodyComposition = bodyCompositionData
                )
                _healthMetricsState.value = HealthMetricsState.Success(_currentMetrics.value)

                Log.d(TAG, "✅ Body composition saved successfully!")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error saving body composition: ${e.message}", e)
                _healthMetricsState.value = HealthMetricsState.Error(
                    e.message ?: "Failed to save body composition"
                )
            }
        }
    }

    // Reset state
    fun resetState() {
        _healthMetricsState.value = HealthMetricsState.Idle
    }
}