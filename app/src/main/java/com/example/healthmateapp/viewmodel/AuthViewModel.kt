package com.example.healthmateapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName

    companion object {
        private const val TAG = "AuthViewModel"
    }

    // ---------------- Login ----------------
    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user ?: throw Exception("User is null after login")
                _currentUser.value = user

                // Load role dan username
                val role = loadUserRole()
                _userRole.value = role

                // Emit success setelah role dan username siap
                _authState.value = AuthState.Success(user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }


    // ---------------- Load user role ----------------
    private suspend fun loadUserRole(): String {
        val uid = auth.currentUser?.uid ?: return "patient"
        return try {
            val docSnapshot = firestore.collection("User").document(uid).get().await()
            val role = docSnapshot.getString("role") ?: "patient"
            _userName.value = docSnapshot.getString("username")
            role
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading user role: ${e.message}", e)
            "patient"
        }
    }

    // ---------------- Register ----------------
    fun register(email: String, password: String, username: String, role: String) {
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Please enter a valid email")
            return
        }

        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d(TAG, "Starting registration for $email with role: $role")

                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("User ID is null")

                // Update display name
                result.user?.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                )?.await()

                // Create user document
                val userData = hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "username" to username,
                    "role" to role,
                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("User").document(uid).set(userData).await()

                // Role-specific profile
                if (role == "patient") createPatientProfile(uid, username)
                else if (role == "assistant") createAssistantProfile(uid, username)

                _userRole.value = role
                _currentUser.value = result.user
                _authState.value = AuthState.Success(result.user)

                Log.d(TAG, "✅ Registration completed successfully!")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Registration error: ${e.message}", e)
                _authState.value = AuthState.Error(
                    e.message ?: "Registration failed. Please try again."
                )
            }
        }
    }

    // ---------------- Create patient profile ----------------
    private suspend fun createPatientProfile(uid: String, username: String) {
        val patientData = hashMapOf(
            "patientId" to uid,
            "name" to username,
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("User")
            .document(uid)
            .collection("patient")
            .document(uid)
            .set(patientData)
            .await()

        val initialHealthMetrics = hashMapOf(
            "initialized" to true,
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("User")
            .document(uid)
            .collection("patient")
            .document(uid)
            .collection("healthMetrics")
            .document("latest")
            .set(initialHealthMetrics)
            .await()
    }

    // ---------------- Create assistant profile ----------------
    private suspend fun createAssistantProfile(uid: String, username: String) {
        val assistantData = hashMapOf(
            "assistantId" to uid,
            "name" to username,
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("User")
            .document(uid)
            .collection("assistant")
            .document(uid)
            .set(assistantData)
            .await()
    }

    // ---------------- Logout ----------------
    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _userRole.value = null
        _authState.value = AuthState.Idle
        Log.d(TAG, "User logged out")
    }

    // ---------------- Reset password ----------------
    fun resetPassword(email: String) {
        if (email.isEmpty()) {
            _authState.value = AuthState.Error("Please enter your email")
            return
        }

        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.Success(null) // atau bisa pakai message khusus
                Log.d(TAG, "Password reset email sent to: $email")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending reset email: ${e.message}", e)
                _authState.value = AuthState.Error(
                    e.message ?: "Failed to send reset email"
                )
            }
        }
    }

    // ---------------- Reset auth state ----------------
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
