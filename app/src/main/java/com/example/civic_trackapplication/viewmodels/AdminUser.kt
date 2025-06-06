package com.example.civic_trackapplication.viewmodels

import com.google.firebase.firestore.DocumentSnapshot

data class AdminUser(
    val uid: String,
    val email: String,
    val isAdmin: Boolean,
    val name: String = "",
    val photoUrl: String? = null
)
fun DocumentSnapshot.toAdminUser(): AdminUser {
    return AdminUser(
        uid = id,
        email = getString("email") ?: "",
        isAdmin = getBoolean("isAdmin") ?: false,
        name =getString("name") ?: "",
        photoUrl = getString("photoUrl")
    )
}