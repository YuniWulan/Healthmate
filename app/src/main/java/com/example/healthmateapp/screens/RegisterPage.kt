package com.example.healthmateapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterClick: (username: String, email: String, password: String, role: String) -> Unit = { _, _, _, _ -> },
    onLoginClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    isLoading: Boolean = false
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    var selectedRole by remember { mutableStateOf("patient") }
    var roleDropdownExpanded by remember { mutableStateOf(false) }
    val roles = listOf("patient" to "Patient", "assistant" to "Assistant")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 60.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Header
            Text(
                text = "Register Now!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "One app for all your health needs.\nMake your lifestyle healthier and more consistent.",
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Username Field
            Text("Username*", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter username", color = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF0A84FF),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedContainerColor = Color(0xFFF5F5F5)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Email Field
            Text("Email*", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter email", color = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF0A84FF),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedContainerColor = Color(0xFFF5F5F5)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password Field
            Text("Password*", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter password", color = Color.Gray) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF0A84FF),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedContainerColor = Color(0xFFF5F5F5)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Role Dropdown
            Text("Role*", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = roleDropdownExpanded,
                onExpandedChange = { if (!isLoading) roleDropdownExpanded = !roleDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = roles.find { it.first == selectedRole }?.second ?: "Patient",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = {
                        Icon(
                            imageVector = if (roleDropdownExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color(0xFF0A84FF),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5)
                    )
                )

                ExposedDropdownMenu(
                    expanded = roleDropdownExpanded,
                    onDismissRequest = { roleDropdownExpanded = false }
                ) {
                    roles.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (value == "patient") Icons.Filled.Person else Icons.Filled.MedicalServices,
                                        contentDescription = null,
                                        tint = Color(0xFF0A84FF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(label)
                                }
                            },
                            onClick = {
                                selectedRole = value
                                roleDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Terms Checkbox + Text
            Row(verticalAlignment = Alignment.Top) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    enabled = !isLoading,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF0A84FF),
                        uncheckedColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                val annotatedText = buildAnnotatedString {
                    append("By registering, you agree to our ")
                    pushStringAnnotation(tag = "TERMS", annotation = "terms")
                    withStyle(SpanStyle(color = Color(0xFF0A84FF))) { append("[Terms of Service]") }
                    pop()
                    append(" and ")
                    pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
                    withStyle(SpanStyle(color = Color(0xFF0A84FF))) { append("[Privacy Policy]") }
                    pop()
                    append(".")
                }

                androidx.compose.foundation.text.ClickableText(
                    text = annotatedText,
                    onClick = { offset ->
                        annotatedText.getStringAnnotations("TERMS", offset, offset).firstOrNull()?.let { onTermsClick() }
                        annotatedText.getStringAnnotations("PRIVACY", offset, offset).firstOrNull()?.let { onPrivacyClick() }
                    },
                    style = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Register Button
            Button(
                onClick = { if (termsAccepted) onRegisterClick(username, email, password, selectedRole) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = termsAccepted && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A84FF),
                    disabledContainerColor = Color(0xFF0A84FF).copy(alpha = 0.5f)
                )
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                else Text("Register", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Already have an account? ", color = Color.Gray, fontSize = 14.sp)
                TextButton(onClick = onLoginClick, contentPadding = PaddingValues(0.dp), enabled = !isLoading) {
                    Text("Sign In", color = Color(0xFF0A84FF), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen()
}