package com.example.healthmateapp.screens

import androidx.compose.ui.tooling.preview.Preview
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterClick: (username: String, email: String, password: String, role: String) -> Unit = { _,_,_,_ -> },
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

    // ROLE
    var selectedRole by remember { mutableStateOf("patient") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White

    ) {
        var roleDropdownExpanded by remember { mutableStateOf(false) }

        val roles = listOf(
            "patient" to "Patient",
            "assistant" to "Assistant"
        )


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 60.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {

            // ===== HEADER =====
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


            // ===== USERNAME =====
            Text("Username*", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter your username", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF0A84FF),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedContainerColor = Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(Modifier.height(20.dp))


            // ===== EMAIL =====
            Text("Email*", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter your email", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF0A84FF),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedContainerColor = Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(Modifier.height(20.dp))


            // ===== PASSWORD =====
            Text("Password*", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter your password", color = Color.Gray) },
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
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF0A84FF),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedContainerColor = Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(Modifier.height(20.dp))
            Spacer(modifier = Modifier.height(20.dp))

            // Role Selection Dropdown
            Text(
                text = "Role*",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = roleDropdownExpanded,
                onExpandedChange = { if (!isLoading) roleDropdownExpanded = !roleDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = roles.find { it.first == selectedRole }?.second ?: "Patient",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        Icon(
                            imageVector = if (roleDropdownExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color(0xFF0A84FF),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                )

                ExposedDropdownMenu(
                    expanded = roleDropdownExpanded,
                    onDismissRequest = { roleDropdownExpanded = false }
                ) {
                    roles.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
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


            // ===== TERMS =====
            Row(verticalAlignment = Alignment.Top) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF0A84FF),
                        uncheckedColor = Color.LightGray
                    )
                )

                Spacer(Modifier.width(8.dp))

                val annotatedText = buildAnnotatedString {
                    append("By registering, you agree to the ")

                    pushStringAnnotation(tag = "TERMS", annotation = "terms")
                    withStyle(style = SpanStyle(color = Color(0xFF0A84FF))) { append("[Terms of Service]") }
                    pop()

                    append(" and ")

                    pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
                    withStyle(style = SpanStyle(color = Color(0xFF0A84FF))) { append("[Privacy Policy]") }
                    pop()

                    append(".")
                }

                androidx.compose.foundation.text.ClickableText(
                    text = annotatedText,
                    onClick = { offset ->
                        annotatedText.getStringAnnotations("TERMS", offset, offset).firstOrNull()?.let { onTermsClick() }
                        annotatedText.getStringAnnotations("PRIVACY", offset, offset).firstOrNull()?.let { onPrivacyClick() }
                    },
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            // ===== REGISTER BUTTON =====
            Button(
                onClick = {
                    if (termsAccepted) {
                        onRegisterClick(username, email, password, selectedRole)

                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A84FF),
                    disabledContainerColor = Color(0xFF0A84FF).copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = termsAccepted && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Register", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ===== LOGIN LINK =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Already have an account? ", color = Color.Gray)
                TextButton(onClick = onLoginClick, contentPadding = PaddingValues(0.dp)) {
                    Text("Login", color = Color(0xFF0A84FF), fontWeight = FontWeight.SemiBold)
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
