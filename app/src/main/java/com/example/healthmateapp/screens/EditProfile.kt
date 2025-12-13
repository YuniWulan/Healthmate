package com.example.healthmateapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    currentUserName: String = "",
    currentEmail: String = "",
    onBackClick: () -> Unit = {},
    onSaveClick: (String, String, String) -> Unit = { _, _, _ -> },
    isLoading: Boolean = false
) {
    var username by remember { mutableStateOf(currentUserName) }
    var email by remember { mutableStateOf(currentEmail) }
    var currentPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Validation functions
    fun validateUsername(value: String): String? {
        return when {
            value.isBlank() -> "Username cannot be empty"
            value.length < 3 -> "Username must be at least 3 characters"
            else -> null
        }
    }

    fun validateEmail(value: String): String? {
        return when {
            value.isBlank() -> "Email cannot be empty"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Invalid email format"
            else -> null
        }
    }

    fun validatePassword(value: String): String? {
        return when {
            value.isBlank() -> "Password is required to save changes"
            value.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Picture Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        tint = Color.Gray,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { /* TODO: Implement image picker */ },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF0A84FF)
                    )
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Photo")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Account Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Username Field
                    Text(
                        text = "Username",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            usernameError = validateUsername(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your username") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Username",
                                tint = Color(0xFF0A84FF)
                            )
                        },
                        isError = usernameError != null,
                        supportingText = {
                            if (usernameError != null) {
                                Text(
                                    text = usernameError!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0A84FF),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Field
                    Text(
                        text = "Email",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = validateEmail(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your email") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = "Email",
                                tint = Color(0xFF0A84FF)
                            )
                        },
                        isError = emailError != null,
                        supportingText = {
                            if (emailError != null) {
                                Text(
                                    text = emailError!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0A84FF),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        singleLine = true,
                        enabled = !isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Verify Your Identity",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Please enter your current password to save changes",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Current Password Field
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                            passwordError = validatePassword(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Current password") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Password",
                                tint = Color(0xFF0A84FF)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password"
                                    else "Show password",
                                    tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        supportingText = {
                            if (passwordError != null) {
                                Text(
                                    text = passwordError!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0A84FF),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        singleLine = true,
                        enabled = !isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        // Validate all fields
                        val userError = validateUsername(username)
                        val mailError = validateEmail(email)
                        val passError = validatePassword(currentPassword)

                        usernameError = userError
                        emailError = mailError
                        passwordError = passError

                        // If no errors, proceed with save
                        if (userError == null && mailError == null && passError == null) {
                            onSaveClick(username, email, currentPassword)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0A84FF)
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Save Changes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    ),
                    enabled = !isLoading
                ) {
                    Text(
                        "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}