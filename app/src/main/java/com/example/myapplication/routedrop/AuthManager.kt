package com.example.myapplication.routedrop

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class AuthManager(context: Context? = null) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getCurrentFirebaseUser(): FirebaseUser? = auth.currentUser

    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank()) {
            onError("Email address is required.")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("AuthManager", "Failed to send password reset email", e)
                onError(e.localizedMessage ?: "Failed to send password reset email")
            }
    }

    fun fetchSavedUser(onResult: (User?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult(null)
            return
        }

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val user = mapDocumentToUser(currentUser.uid, doc.data ?: emptyMap())
                    onResult(user)
                } else {
                    val email = currentUser.email ?: ""
                    val phone = currentUser.phoneNumber ?: ""
                    val name = currentUser.displayName ?: "User"
                    val fallbackUser = Customer(
                        userId = currentUser.uid,
                        name = name,
                        password = "",
                        emailAddress = email,
                        phoneNumber = phone,
                        address = ""
                    )
                    onResult(fallbackUser)
                }
            }
            .addOnFailureListener { e ->
                Log.e("AuthManager", "Failed to fetch user profile from Firestore", e)
                onResult(null)
            }
    }

    fun signUp(
        user: User,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        if (user.emailAddress.isBlank()) {
            onError("Email address is required for Firebase Authentication.")
            return
        }

        auth.createUserWithEmailAndPassword(user.emailAddress, user.password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: user.userId
                val newUser: User = when (user) {
                    is Courier -> Courier(
                        userId = uid,
                        name = user.name,
                        emailAddress = user.emailAddress,
                        password = user.password,
                        phoneNumber = user.phoneNumber,
                        address = user.address,
                        vehicleType = user.vehicleType
                    )
                    is Recipient -> Recipient(
                        userId = uid,
                        name = user.name,
                        password = user.password,
                        emailAddress = user.emailAddress,
                        phoneNumber = user.phoneNumber,
                        address = user.address
                    )
                    else -> Customer(
                        userId = uid,
                        name = user.name,
                        password = user.password,
                        emailAddress = user.emailAddress,
                        phoneNumber = user.phoneNumber,
                        address = user.address
                    )
                }

                saveUserToFirestore(newUser) { success, err ->
                    if (success) {
                        onSuccess(newUser)
                    } else {
                        onError(err ?: "Sign up succeeded but failed to save user profile.")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("AuthManager", "Firebase sign up failed", e)
                onError(e.localizedMessage ?: "Sign up failed")
            }
    }

    fun signIn(
        identifier: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        val trimmed = identifier.trim()
        if (trimmed.isBlank()) {
            onError("Please enter your email or phone number.")
            return
        }

        if (trimmed.contains("@")) {
            performFirebaseSignIn(trimmed, password, onSuccess, onError)
        } else {
            db.collection("users")
                .whereEqualTo("phoneNumber", trimmed)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val email = querySnapshot.documents.firstOrNull()?.getString("emailAddress")
                    if (!email.isNullOrBlank()) {
                        performFirebaseSignIn(email, password, onSuccess, onError)
                    } else {
                        performFirebaseSignIn(trimmed, password, onSuccess, onError)
                    }
                }
                .addOnFailureListener {
                    performFirebaseSignIn(trimmed, password, onSuccess, onError)
                }
        }
    }

    private fun performFirebaseSignIn(
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc != null && doc.exists()) {
                            val user = mapDocumentToUser(uid, doc.data ?: emptyMap())
                            onSuccess(user)
                        } else {
                            val user = Customer(
                                userId = uid,
                                name = authResult.user?.displayName ?: "User",
                                password = password,
                                emailAddress = authResult.user?.email ?: email,
                                phoneNumber = authResult.user?.phoneNumber ?: "",
                                address = ""
                            )
                            onSuccess(user)
                        }
                    }
                    .addOnFailureListener { e ->
                        onError("Failed to retrieve user details: ${e.localizedMessage}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("AuthManager", "Firebase sign in failed", e)
                val cleanError = if (e.localizedMessage?.contains("incorrect, malformed or has expired", ignoreCase = true) == true) {
                    "Incorrect password."
                } else {
                    e.localizedMessage ?: "Invalid credentials or sign in failed"
                }
                onError(cleanError)
            }
    }

    fun saveUser(user: User, onComplete: ((Boolean) -> Unit)? = null) {
        saveUserToFirestore(user) { success, _ ->
            onComplete?.invoke(success)
        }
    }

    private fun saveUserToFirestore(
        user: User,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val data = mutableMapOf<String, Any>(
            "userId" to user.userId,
            "name" to user.name,
            "emailAddress" to user.emailAddress,
            "phoneNumber" to user.phoneNumber,
            "address" to user.address,
            "role" to user.role()
        )
        if (user is Courier) {
            data["vehicleType"] = user.vehicleType
        }

        db.collection("users").document(user.userId).set(data)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("AuthManager", "Error saving user to Firestore", e)
                onComplete(false, e.localizedMessage)
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun clearUser() {
        signOut()
    }

    private fun mapDocumentToUser(uid: String, data: Map<String, Any>): User {
        val role = (data["role"] as? String) ?: "CUSTOMER"
        val name = (data["name"] as? String) ?: ""
        val email = (data["emailAddress"] as? String) ?: ""
        val phone = (data["phoneNumber"] as? String) ?: ""
        val address = (data["address"] as? String) ?: ""
        val vehicle = (data["vehicleType"] as? String) ?: "Motorcycle"

        return when (role) {
            "COURIER" -> Courier(uid, name, email, "", phone, address, vehicle)
            "RECIPIENT" -> Recipient(uid, name, "", email, phone, address)
            else -> Customer(uid, name, "", email, phone, address)
        }
    }
}
