package com.example.civic_trackapplication.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> = _isAdmin

    fun checkAdminStatus() {
        auth.currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    _isAdmin.value = document.getBoolean("isAdmin") ?: false
                }
                .addOnFailureListener {
                    _isAdmin.value = false
                }
        } ?: run {
            _isAdmin.value = false
        }
    }
}