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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
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
import com.example.myapplication.routedrop.Courier
import com.example.myapplication.routedrop.Customer
import com.example.myapplication.routedrop.Recipient
import com.example.myapplication.routedrop.User
import java.util.UUID

@Composable
fun LoginScreen(
    registeredUsers: List<User>,
    onLoginSuccess: (User) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onGuestLogin: () -> Unit
) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("RouteDrop", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Log In", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (phoneNumber.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val user = registeredUsers.find { it.phoneNumber == phoneNumber && it.password == password }
                    if (user != null) {
                        onLoginSuccess(user)
                    } else {
                        Toast.makeText(context, "Invalid phone number or password.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log In")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToSignUp) {
                Text("Don't have an account? Sign Up")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onGuestLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue as Guest")
            }
        }
    }
}

@Composable
fun SignUpScreen(
    onSignUpSuccess: (User) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var selectedRole by remember { mutableStateOf("CUSTOMER") }
    var selectedVehicle by remember { mutableStateOf("Motorcycle") }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var emailErrorMessage by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("RouteDrop", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Sign Up", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(24.dp))

            // Role Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Role: ", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedRole == "CUSTOMER",
                        onClick = { selectedRole = "CUSTOMER" }
                    )
                    Text("Customer")
                }

                Spacer(modifier = Modifier.width(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedRole == "COURIER",
                        onClick = { selectedRole = "COURIER" }
                    )
                    Text("Courier")
                }

                Spacer(modifier = Modifier.width(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedRole == "RECIPIENT",
                        onClick = { selectedRole = "RECIPIENT" }
                    )
                    Text("Recipient")
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
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
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
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    passwordError = false
                },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            if (passwordError) {
                Text(
                    text = "Password cannot be blank.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    emailError = false
                    passwordError = false
                    var hasError = false
                    
                    if (password.isBlank()) {
                        passwordError = true
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
                        Toast.makeText(context, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val newUser: User = if (selectedRole == "CUSTOMER") {
                        Customer(
                            userId = "C-${UUID.randomUUID().toString().take(6)}",
                            name = name,
                            phoneNumber = phoneNumber,
                            emailAddress = emailAddress.ifBlank { "N/A" },
                            address = address,
                            password = password
                        )
                    } else if (selectedRole == "COURIER") {
                        Courier(
                            userId = "CR-${UUID.randomUUID().toString().take(6)}",
                            name = name,
                            phoneNumber = phoneNumber,
                            emailAddress = emailAddress.ifBlank { "N/A" },
                            address = address,
                            password = password,
                            vehicleType = selectedVehicle
                        )
                    } else {
                        Recipient(
                            userId = "R-${UUID.randomUUID().toString().take(6)}",
                            name = name,
                            phoneNumber = phoneNumber,
                            emailAddress = emailAddress.ifBlank { "N/A" },
                            address = address,
                            password = password
                        )
                    }

                    Toast.makeText(context, "Sign Up Successful!", Toast.LENGTH_SHORT).show()
                    onSignUpSuccess(newUser)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Up")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Log In")
            }
        }
    }
}