package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.routedrop.AuthManager
import com.example.myapplication.routedrop.Customer
import com.example.myapplication.routedrop.Delivery
import com.example.myapplication.ui.LoginScreen
import com.example.myapplication.map.OsmSetup
import com.example.myapplication.routedrop.RouteDropScreen
import com.example.myapplication.ui.SignUpScreen
import com.example.myapplication.routedrop.User
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.routedrop.AppViewModel
import com.example.myapplication.ui.MyApplicationTheme

enum class Screen {
    LOGIN, SIGNUP, HOME
}

class MyApplication : android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        OsmSetup.init(this)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val context = LocalContext.current
                    val authManager = remember { AuthManager(context) }

                    val appViewModel: AppViewModel = viewModel()
                    val registeredUsers = appViewModel.registeredUsers

                    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
                    var loggedInUser by remember { mutableStateOf<User?>(null) }

                    // Auto-login on launch
                    LaunchedEffect(Unit) {
                        authManager.getSavedUser()?.let { savedUser ->
                            registeredUsers.add(savedUser)
                            loggedInUser = savedUser
                            currentScreen = Screen.HOME
                        }
                    }

                    when (currentScreen) {
                        Screen.LOGIN -> {
                            LoginScreen(
                                registeredUsers = registeredUsers,
                                onLoginSuccess = { user ->
                                    authManager.saveUser(user)
                                    loggedInUser = user
                                    currentScreen = Screen.HOME
                                },
                                onNavigateToSignUp = {
                                    currentScreen = Screen.SIGNUP
                                },
                                onGuestLogin = {
                                    val guestUser = Customer(
                                        userId = "guest_${System.currentTimeMillis()}",
                                        name = "Guest User",
                                        password = "",
                                        emailAddress = "guest@example.com",
                                        phoneNumber = "Not Provided",
                                        address = "Exploring Mode"
                                    )
                                    // Don't save guest in SharedPreferences so it doesn't auto-login next time
                                    loggedInUser = guestUser
                                    currentScreen = Screen.HOME
                                }
                            )
                        }
                        Screen.SIGNUP -> {
                            SignUpScreen(
                                onSignUpSuccess = { newUser ->
                                    authManager.saveUser(newUser)
                                    registeredUsers.add(newUser)
                                    loggedInUser = newUser
                                    currentScreen = Screen.HOME
                                },
                                onNavigateToLogin = {
                                    currentScreen = Screen.LOGIN
                                }
                            )
                        }
                        Screen.HOME -> {
                            loggedInUser?.let { user ->
                                RouteDropScreen(
                                    loggedInUser = user,
                                    viewModel = appViewModel,
                                    onLogout = {
                                        authManager.clearUser()
                                        loggedInUser = null
                                        currentScreen = Screen.LOGIN
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
