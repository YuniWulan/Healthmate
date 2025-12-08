package com.example.healthmateapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NewPasswordScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    /**
     * Callback yang melakukan update password di backend.
     * Return Result.success(Unit) jika berhasil, atau Result.failure(Exception) bila gagal.
     * Defaultnya mensimulasikan delay 1 detik dan selalu berhasil jika panjang >= 8.
     */
    onUpdatePassword: suspend (newPassword: String) -> Result<Unit> = { newPassword ->
        // simulasi network
        delay(1000)
        if (newPassword.length >= 8) Result.success(Unit)
        else Result.failure(Exception("Password terlalu pendek"))
    }
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }

    fun validatePassword(pw: String): String? {
        if (pw.isBlank()) return "Kata sandi tidak boleh kosong"
        if (pw.length < 8) return "Minimal 8 karakter"
        // opsional: cek kombinasi huruf & angka
        // if (!pw.any { it.isDigit() } || !pw.any { it.isLetter() }) return "Gunakan huruf dan angka"
        return null
    }

    fun validateConfirm(pw: String, conf: String): String? {
        if (conf.isBlank()) return "Konfirmasi kata sandi tidak boleh kosong"
        if (pw != conf) return "Kata sandi tidak cocok"
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
            Text(
                text = "Buat Kata Sandi Baru",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Masukkan kata sandi baru Anda dan konfirmasi. Minimal 8 karakter.",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Kata Sandi Baru*") },
                placeholder = { Text("Masukkan kata sandi baru", color = Color.Gray) },
                textStyle = TextStyle(color = Color(0xFF1A1A1A)),
                singleLine = true,
                isError = passwordError != null,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Sembunyikan kata sandi" else "Tampilkan kata sandi"
                        )
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    cursorColor = Color(0xFF1E88E5)
                )
            )

            if (passwordError != null) {
                Text(
                    text = passwordError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm password field
            OutlinedTextField(
                value = confirm,
                onValueChange = {
                    confirm = it
                    confirmError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Konfirmasi Kata Sandi*") },
                placeholder = { Text("Ketik ulang kata sandi", color = Color.Gray) },
                textStyle = TextStyle(color = Color(0xFF1A1A1A)),
                singleLine = true,
                isError = confirmError != null,
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        confirmError = validateConfirm(password, confirm)
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(
                            imageVector = if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmVisible) "Sembunyikan konfirmasi" else "Tampilkan konfirmasi"
                        )
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    cursorColor = Color(0xFF1E88E5)
                )
            )

            if (confirmError != null) {
                Text(
                    text = confirmError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    val pwErr = validatePassword(password)
                    val confErr = validateConfirm(password, confirm)
                    passwordError = pwErr
                    confirmError = confErr

                    if (pwErr == null && confErr == null) {
                        loading = true
                        scope.launch {
                            try {
                                val res = onUpdatePassword(password)
                                loading = false
                                if (res.isSuccess) {
                                    snackbarHostState.showSnackbar("Password berhasil diubah")
                                    // Kembali ke login atau screen lain
                                    navController.popBackStack()
                                } else {
                                    snackbarHostState.showSnackbar("Gagal: ${res.exceptionOrNull()?.localizedMessage}")
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
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = "Simpan Kata Sandi", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NewPasswordPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        NewPasswordScreen(navController = navController)
    }
}