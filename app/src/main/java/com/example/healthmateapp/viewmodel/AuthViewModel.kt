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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.userProfileChangeRequest

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

    companion object {
        private const val TAG = "AuthViewModel"
    }

    init {
        // Check if user is already logged in
        _currentUser.value = auth.currentUser
        if (auth.currentUser != null) {
            _authState.value = AuthState.Success(auth.currentUser)
            loadUserRole()
        }
    }

    // Load user role from Firestore
    private fun loadUserRole() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val docSnapshot = firestore.collection("User")
                    .document(uid)
                    .get()
                    .await()

                val role = docSnapshot.getString("role") ?: "patient"
                _userRole.value = role
                Log.d(TAG, "Loaded user role: $role")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user role: ${e.message}", e)
                _userRole.value = "patient" // Default to patient
            }
        }
    }

    // Update user profile (username and email)
    fun updateProfile(newUsername: String, newEmail: String, currentPassword: String) {
        val user = auth.currentUser

        if (user == null) {
            _authState.value = AuthState.Error("No user logged in")
            return
        }

        if (newUsername.isBlank() || newEmail.isBlank() || currentPassword.isBlank()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }

        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d(TAG, "Starting profile update for user: ${user.uid}")

                // Re-authenticate user before making sensitive changes
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                user.reauthenticate(credential).await()
                Log.d(TAG, "User re-authenticated successfully")

                // Update email if changed
                if (newEmail != user.email) {
                    user.updateEmail(newEmail).await()
                    Log.d(TAG, "Email updated to: $newEmail")
                }

                // Update display name if changed
                if (newUsername != user.displayName) {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = newUsername
                    }
                    user.updateProfile(profileUpdates).await()
                    Log.d(TAG, "Display name updated to: $newUsername")
                }

                // Update Firestore User collection
                val updates = hashMapOf<String, Any>(
                    "username" to newUsername,
                    "email" to newEmail,
                    "updatedAt" to System.currentTimeMillis()
                )

                firestore.collection("User")
                    .document(user.uid)
                    .update(updates)
                    .await()

                Log.d(TAG, "Firestore User document updated")

                // Update patient or assistant subcollection name
                val role = _userRole.value ?: "patient"
                if (role == "patient") {
                    firestore.collection("User")
                        .document(user.uid)
                        .collection("patient")
                        .document(user.uid)
                        .update(mapOf("name" to newUsername))
                        .await()
                    Log.d(TAG, "Patient profile name updated")
                } else if (role == "assistant") {
                    firestore.collection("User")
                        .document(user.uid)
                        .collection("assistant")
                        .document(user.uid)
                        .update(mapOf("name" to newUsername))
                        .await()
                    Log.d(TAG, "Assistant profile name updated")
                }

                // Refresh current user
                auth.currentUser?.reload()?.await()
                _currentUser.value = auth.currentUser
                _authState.value = AuthState.Success(auth.currentUser)

                Log.d(TAG, "✅ Profile update completed successfully!")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Profile update error: ${e.message}", e)
                val errorMessage = when {
                    e.message?.contains("password is invalid") == true ||
                            e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                        "Current password is incorrect"
                    e.message?.contains("email address is already") == true ->
                        "This email is already in use"
                    e.message?.contains("requires recent authentication") == true ->
                        "Please log in again to update your profile"
                    e.message?.contains("network") == true ->
                        "Network error. Please check your connection"
                    else -> e.message ?: "Failed to update profile"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    // Register new user with email, password, and role
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

                // Create user with email and password
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("User ID is null")

                Log.d(TAG, "Firebase Auth user created with UID: $uid")

                // Update display name
                result.user?.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                )?.await()

                Log.d(TAG, "Display name updated to: $username")

                // Create user document in Firestore
                val userData = hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "username" to username,
                    "role" to role,
                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("User")
                    .document(uid)
                    .set(userData)
                    .await()

                Log.d(TAG, "✅ User document created in Firestore")

                // Create role-specific subcollection based on role
                if (role == "patient") {
                    createPatientProfile(uid, username)
                } else if (role == "assistant") {
                    createAssistantProfile(uid, username)
                }

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

    // Create patient profile structure
    private suspend fun createPatientProfile(uid: String, username: String) {
        try {
            Log.d(TAG, "Creating patient profile for UID: $uid")

            // Create patient subcollection with patient document
            val patientData = hashMapOf(
                "patientId" to uid,
                "name" to username,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("User")
                .document(uid)
                .collection("patient")
                .document(uid) // Using uid as patient_id
                .set(patientData)
                .await()

            Log.d(TAG, "✅ Patient profile created at: User/$uid/patient/$uid")

            // Initialize empty healthMetrics document
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

            Log.d(TAG, "✅ Health metrics initialized")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating patient profile: ${e.message}", e)
            throw e
        }
    }

    // Create assistant profile structure
    private suspend fun createAssistantProfile(uid: String, username: String) {
        try {
            Log.d(TAG, "Creating assistant profile for UID: $uid")

            // Create assistant subcollection with assistant document
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

            Log.d(TAG, "✅ Assistant profile created at: User/$uid/assistant/$uid")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating assistant profile: ${e.message}", e)
            throw e
        }
    }

    // Login with email and password
    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d(TAG, "Attempting login for: $email")

                // Sign in with email and password
                val result = auth.signInWithEmailAndPassword(email, password).await()

                _currentUser.value = result.user
                loadUserRole() // Load user role after login
                _authState.value = AuthState.Success(result.user)

                Log.d(TAG, "✅ Login successful!")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Login error: ${e.message}", e)
                _authState.value = AuthState.Error(
                    e.message ?: "Login failed. Please check your credentials."
                )
            }
        }
    }

    // Logout
    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _userRole.value = null
        _authState.value = AuthState.Idle
        Log.d(TAG, "User logged out")
    }

    // Reset password
    fun resetPassword(email: String) {
        if (email.isEmpty()) {
            _authState.value = AuthState.Error("Please enter your email")
            return
        }

        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.Success(null)
                Log.d(TAG, "Password reset email sent to: $email")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending reset email: ${e.message}", e)
                _authState.value = AuthState.Error(
                    e.message ?: "Failed to send reset email"
                )
            }
        }
    }

    // Reset auth state to idle
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}