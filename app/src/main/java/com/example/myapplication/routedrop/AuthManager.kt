package com.example.myapplication.routedrop

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("route_drop_auth", Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        prefs.edit().apply {
            putString("userId", user.userId)
            putString("name", user.name)
            putString("emailAddress", user.emailAddress)
            putString("password", user.password)
            putString("phoneNumber", user.phoneNumber)
            putString("address", user.address)
            putString("role", user.role())
            if (user is Courier) {
                putString("vehicleType", user.vehicleType)
            }
            putBoolean("isLoggedIn", true)
            apply()
        }
    }

    fun getSavedUser(): User? {
        if (!prefs.getBoolean("isLoggedIn", false)) return null
        
        val role = prefs.getString("role", "") ?: ""
        val userId = prefs.getString("userId", "") ?: ""
        val name = prefs.getString("name", "") ?: ""
        val email = prefs.getString("emailAddress", "") ?: ""
        val pass = prefs.getString("password", "") ?: ""
        val phone = prefs.getString("phoneNumber", "") ?: ""
        val address = prefs.getString("address", "") ?: ""
        val vehicle = prefs.getString("vehicleType", "Motorcycle") ?: "Motorcycle"
        
        return if (role == "CUSTOMER") {
            Customer(userId, name, pass, email, phone, address)
        } else if (role == "COURIER") {
            Courier(userId, name, email, pass, phone, address, vehicle)
        } else if (role == "RECIPIENT") {
            Recipient(userId, name, pass, email, phone, address)
        } else {
            null
        }
    }

    fun clearUser() {
        prefs.edit().clear().apply()
    }
}
