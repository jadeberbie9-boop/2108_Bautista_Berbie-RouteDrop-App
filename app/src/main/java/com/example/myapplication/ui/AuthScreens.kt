package com.example.myapplication.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.routedrop.AuthManager
import com.example.myapplication.routedrop.Courier
import com.example.myapplication.routedrop.Customer
import com.example.myapplication.routedrop.User
import java.util.UUID

@Composable
fun LoginScreen(
    authManager: AuthManager,
    onLoginSuccess: (User) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onGuestLogin: () -> Unit
) {
    val context = LocalContext.current
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            authManager = authManager,
            onDismiss = { showForgotPasswordDialog = false }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = "App Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("RouteDrop", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Your delivery, routed right.", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = identifier,
                onValueChange = { 
                    identifier = it
                    errorMessage = null
                },
                label = { Text("Email or Phone Number") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    errorMessage = null
                },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (identifier.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all fields."
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null
                    authManager.signIn(
                        identifier = identifier,
                        password = password,
                        onSuccess = { user ->
                            isLoading = false
                            onLoginSuccess(user)
                        },
                        onError = { err ->
                            isLoading = false
                            errorMessage = err
                        }
                    )
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Log In")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { showForgotPasswordDialog = true },
                enabled = !isLoading,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Forgot Password?")
            }

            TextButton(
                onClick = onNavigateToSignUp,
                enabled = !isLoading,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Don't have an account? Sign Up")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                Text("OR", modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onGuestLogin,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue as Guest")
            }
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    authManager: AuthManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password") },
        text = {
            Column {
                if (isSuccess) {
                    Text("Password reset email sent! Please check your inbox.")
                } else {
                    Text("Enter your email address to receive a password reset link.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isSuccess) {
                Button(onClick = onDismiss) {
                    Text("OK")
                }
            } else {
                Button(
                    onClick = {
                        if (email.isBlank()) {
                            errorMessage = "Please enter your email."
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        authManager.sendPasswordResetEmail(
                            email = email,
                            onSuccess = {
                                isLoading = false
                                isSuccess = true
                                Toast.makeText(context, "Reset link sent", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                isLoading = false
                                errorMessage = err
                            }
                        )
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Send Reset Link")
                    }
                }
            }
        },
        dismissButton = {
            if (!isSuccess) {
                TextButton(onClick = onDismiss, enabled = !isLoading) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun SignUpScreen(
    authManager: AuthManager,
    onSignUpSuccess: (User) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var selectedRole by remember { mutableStateOf("CUSTOMER") }
    var selectedVehicle by remember { mutableStateOf("Motorcycle") }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var emailErrorMessage by remember { mutableStateOf("") }
    var passwordErrorMessage by remember { mutableStateOf("") }
    var generalErrorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("RouteDrop", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Sign Up with Firebase Auth", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(24.dp))

            // Role Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Role: ", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedRole == "CUSTOMER",
                        onClick = { selectedRole = "CUSTOMER" }
                    )
                    Text("Customer")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedRole == "COURIER",
                        onClick = { selectedRole = "COURIER" }
                    )
                    Text("Courier")
                }
            }
            
            if (selectedRole == "COURIER") {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Vehicle Type:", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Motorcycle", "Car", "Truck").forEach { vehicle ->
                        OutlinedButton(
                            onClick = { selectedVehicle = vehicle },
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedVehicle == vehicle) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(vehicle)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { 
                    name = it
                    generalErrorMessage = null
                },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { 
                    phoneNumber = it
                    generalErrorMessage = null
                },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = emailAddress,
                onValueChange = { 
                    emailAddress = it
                    emailError = false
                    generalErrorMessage = null
                },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                isError = emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            if (emailError) {
                Text(
                    text = emailErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { 
                    address = it
                    generalErrorMessage = null
                },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    passwordError = false
                    generalErrorMessage = null
                },
                label = { Text("Password") },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = passwordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            if (passwordError) {
                Text(
                    text = passwordErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            if (generalErrorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = generalErrorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    emailError = false
                    passwordError = false
                    generalErrorMessage = null
                    var hasError = false
                    
                    if (password.isBlank()) {
                        passwordError = true
                        passwordErrorMessage = "Password cannot be blank."
                        hasError = true
                    } else if (!isPasswordValid(password)) {
                        passwordError = true
                        passwordErrorMessage = if (password.length < 8 && !password.any { it.isDigit() }) {
                            "Password must be at least 8 characters and include numbers."
                        } else if (password.length < 8) {
                            "Password must be at least 8 characters long."
                        } else {
                            "Password must contain at least one number."
                        }
                        hasError = true
                    }
                    
                    if (emailAddress.isNotBlank()) {
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
                            emailError = true
                            emailErrorMessage = "Invalid email format."
                            hasError = true
                        }
                    } else {
                        emailError = true
                        emailErrorMessage = "Email cannot be blank."
                        hasError = true
                    }
                    
                    if (hasError) return@Button

                    if (name.isBlank() || phoneNumber.isBlank() || address.isBlank()) {
                        generalErrorMessage = "Please fill in all required fields."
                        return@Button
                    }

                    val placeholderId = "temp_${UUID.randomUUID()}"
                    val newUser: User = if (selectedRole == "COURIER") {
                        Courier(
                            userId = placeholderId,
                            name = name,
                            phoneNumber = phoneNumber,
                            emailAddress = emailAddress,
                            address = address,
                            password = password,
                            vehicleType = selectedVehicle
                        )
                    } else {
                        Customer(
                            userId = placeholderId,
                            name = name,
                            phoneNumber = phoneNumber,
                            emailAddress = emailAddress,
                            address = address,
                            password = password
                        )
                    }

                    isLoading = true
                    authManager.signUp(
                        user = newUser,
                        onSuccess = { createdUser ->
                            isLoading = false
                            Toast.makeText(context, "Sign Up Successful!", Toast.LENGTH_SHORT).show()
                            onSignUpSuccess(createdUser)
                        },
                        onError = { err ->
                            isLoading = false
                            generalErrorMessage = err
                        }
                    )
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Sign Up")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onNavigateToLogin,
                enabled = !isLoading
            ) {
                Text("Already have an account? Log In")
            }
        }
    }
}

fun isPasswordValid(password: String): Boolean {
    return password.length >= 8 && password.any { it.isDigit() }
}
