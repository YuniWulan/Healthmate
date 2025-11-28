package com.example.healthmateapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
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

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    init {
        // Check if user is already logged in
        _currentUser.value = auth.currentUser
        if (auth.currentUser != null) {
            _authState.value = AuthState.Success(auth.currentUser)
        }
    }

    // Register new user with email and password
    fun register(email: String, password: String, username: String) {
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

                // Create user with email and password
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                // Update display name
                result.user?.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                )?.await()

                _currentUser.value = result.user
                _authState.value = AuthState.Success(result.user)

            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.message ?: "Registration failed. Please try again."
                )
            }
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

                // Sign in with email and password
                val result = auth.signInWithEmailAndPassword(email, password).await()

                _currentUser.value = result.user
                _authState.value = AuthState.Success(result.user)

            } catch (e: Exception) {
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
        _authState.value = AuthState.Idle
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
                _authState.value = AuthState.Error("Password reset email sent! Check your inbox.")
            } catch (e: Exception) {
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