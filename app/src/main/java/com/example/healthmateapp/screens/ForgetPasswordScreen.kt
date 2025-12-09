package com.example.healthmateapp.screens

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ForgetPasswordScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onSendReset: suspend (email: String) -> Result<Unit> = { email ->
        delay(1000)
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) Result.success(Unit)
        else Result.failure(Exception("Email tidak valid"))
    }
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    fun validateEmail(input: String): String? {
        if (input.isBlank()) return "Email tidak boleh kosong"
        if (!Patterns.EMAIL_ADDRESS.matcher(input).matches()) return "Masukkan email yang valid"
        return null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Header
            Text(
                text = "Lupa Kata Sandi ?",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Masukkan email terdaftar Anda untuk menerima kode verifikasi untuk mengatur ulang kata sandi Anda.",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email Field dengan gaya modern
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                placeholder = { Text("Masukkan email yang terdaftar", color = Color.Gray) },
                textStyle = TextStyle(color = Color(0xFF1A1A1A)),
                singleLine = true,
                isError = emailError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        emailError = validateEmail(email)
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    cursorColor = Color(0xFF1E88E5)
                )
            )

            if (emailError != null) {
                Text(
                    text = emailError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Tombol Kirim Kode
            Button(
                onClick = {
                    focusManager.clearFocus()
                    val v = validateEmail(email)
                    emailError = v
                    if (v == null) {
                        loading = true
                        scope.launch {
                            try {
                                val res = onSendReset(email)
                                loading = false
                                if (res.isSuccess) {
                                    snackbarHostState.showSnackbar("Kode verifikasi dikirim ke $email")
                                    navController.navigate("NewPasswordScreen")
                                } else {
                                    snackbarHostState.showSnackbar("Gagal: ${res.exceptionOrNull()?.localizedMessage ?: "Terjadi kesalahan"}")
                                }
                            } catch (e: Exception) {
                                loading = false
                                snackbarHostState.showSnackbar("Terjadi kesalahan: ${e.localizedMessage}")
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3D8AFF),
                    contentColor = Color.White
                ),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = "Kirim Kode", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgetPasswordPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        ForgetPasswordScreen(navController = navController)
    }
}