package com.example.healthmateapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Data class untuk pasien
data class Patient(
    val id: String,
    val username: String,
    val email: String,
    val age: Int = 0,
    val illness: String = ""
)

class AssistantViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _patientList = MutableLiveData<List<Patient>>()
    val patientList: LiveData<List<Patient>> = _patientList

    fun loadPatients() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e("AssistantVM", "User not logged in")
            return
        }

        firestore.collection("User")
            .document(uid)
            .collection("patient")
            .get()
            .addOnSuccessListener { snapshot ->
                val patients = snapshot.documents.mapNotNull { doc ->
                    Patient(
                        id = doc.id,
                        username = doc.getString("username") ?: "",
                        email = doc.getString("email") ?: "",
                        age = doc.getLong("age")?.toInt() ?: 0,
                        illness = doc.getString("illness") ?: ""
                    )
                }
                _patientList.postValue(patients)
            }
            .addOnFailureListener { e ->
                Log.e("AssistantVM", "Error loading patients", e)
            }
    }
}
