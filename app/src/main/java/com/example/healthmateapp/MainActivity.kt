package com.example.healthmateapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthmateapp.screens.LoginScreen
import com.example.healthmateapp.screens.RegisterScreen
import com.example.healthmateapp.screens.HomeScreen
import com.example.healthmateapp.viewmodel.AuthViewModel
import com.example.healthmateapp.viewmodel.AuthState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object ForgotPassword : Screen("forgot_password")
    object Terms : Screen("terms")
    object Privacy : Screen("privacy")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()

    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Check authentication state and navigate accordingly
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                val user = (authState as AuthState.Success).user
                if (user != null) {
                    Toast.makeText(
                        context,
                        "Welcome ${user.displayName ?: user.email}!",
                        Toast.LENGTH_SHORT
                    ).show()

                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                    authViewModel.resetAuthState()
                }
            }
            is AuthState.Error -> {
                val message = (authState as AuthState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                authViewModel.resetAuthState()
            }
            else -> { /* Do nothing */ }
        }
    }

    // Check if user is already logged in at app start
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // ============ LOGIN SCREEN ============
        composable(Screen.Login.route) {
            val isLoading = authState is AuthState.Loading

            LoginScreen(
                onLoginClick = { email, password ->
                    authViewModel.login(email, password)
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                },
                onForgotPasswordClick = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                isLoading = isLoading
            )
        }

        // ============ REGISTER SCREEN ============
        composable(Screen.Register.route) {
            val isLoading = authState is AuthState.Loading

            RegisterScreen(
                onRegisterClick = { username, email, password ->
                    authViewModel.register(email, password, username)
                },
                onLoginClick = {
                    navController.popBackStack()
                },
                onTermsClick = {
                    navController.navigate(Screen.Terms.route)
                },
                onPrivacyClick = {
                    navController.navigate(Screen.Privacy.route)
                },
                isLoading = isLoading
            )
        }

        // ============ HOME SCREEN ============
        composable(Screen.Home.route) {
            HomeScreen(
                userName = currentUser?.displayName ?: "User",
                onNotificationClick = {
                    Toast.makeText(context, "Notifications", Toast.LENGTH_SHORT).show()
                },
                onProfileClick = {
                    Toast.makeText(context, "Profile", Toast.LENGTH_SHORT).show()
                },
                onReminderClick = { reminder ->
                    Toast.makeText(
                        context,
                        "Reminder: ${reminder.title} at ${reminder.time}",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onRecordConsumptionClick = {
                    Toast.makeText(context, "Record Consumption", Toast.LENGTH_SHORT).show()
                }
//                onLogoutClick = {
//                    authViewModel.logout()
//                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
//                    navController.navigate(Screen.Login.route) {
//                        popUpTo(Screen.Home.route) { inclusive = true }
//                    }
//                }
            )
        }

        // ============ FORGOT PASSWORD SCREEN ============
        composable(Screen.ForgotPassword.route) {
            val isLoading = authState is AuthState.Loading

            ForgotPasswordScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onResetClick = { email ->
                    authViewModel.resetPassword(email)
                },
                isLoading = isLoading
            )
        }

        // ============ TERMS SCREEN ============
        composable(Screen.Terms.route) {
            TermsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // ============ PRIVACY SCREEN ============
        composable(Screen.Privacy.route) {
            PrivacyScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// ============ ADDITIONAL SCREENS ============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onResetClick: (String) -> Unit,
    isLoading: Boolean = false
) {
    var email by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lupa Kata Sandi") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Masukkan email Anda untuk menerima link reset kata sandi",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onResetClick(email) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Kirim Link Reset")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Syarat Layanan") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                text = "Syarat Layanan",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = """
                    1. Penerimaan Syarat
                    Dengan menggunakan aplikasi HealthMate, Anda menyetujui syarat dan ketentuan ini.
                    
                    2. Penggunaan Layanan
                    Anda setuju untuk menggunakan layanan ini hanya untuk tujuan yang sah dan sesuai hukum.
                    
                    3. Akun Pengguna
                    Anda bertanggung jawab untuk menjaga kerahasiaan akun dan kata sandi Anda.
                    
                    4. Konten Pengguna
                    Anda mempertahankan hak atas konten yang Anda unggah ke aplikasi.
                    
                    5. Perubahan Layanan
                    Kami berhak mengubah atau menghentikan layanan kapan saja.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kebijakan Privasi") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                text = "Kebijakan Privasi",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = """
                    1. Informasi yang Kami Kumpulkan
                    Kami mengumpulkan informasi yang Anda berikan saat mendaftar dan menggunakan aplikasi.
                    
                    2. Penggunaan Informasi
                    Informasi Anda digunakan untuk menyediakan dan meningkatkan layanan kami.
                    
                    3. Keamanan Data
                    Kami menggunakan langkah-langkah keamanan standar industri untuk melindungi data Anda.
                    
                    4. Berbagi Informasi
                    Kami tidak menjual atau membagikan informasi pribadi Anda kepada pihak ketiga.
                    
                    5. Hak Anda
                    Anda memiliki hak untuk mengakses, memperbarui, atau menghapus informasi pribadi Anda.
                    
                    6. Cookies
                    Kami menggunakan cookies untuk meningkatkan pengalaman pengguna.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}