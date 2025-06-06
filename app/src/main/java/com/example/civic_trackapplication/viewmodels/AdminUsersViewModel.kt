package com.example.civic_trackapplication.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class AdminUsersViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _users = MutableLiveData<List<AdminUser>>()
    private val _updateStatus = MutableLiveData<Resource<Unit>>()

    val users: LiveData<List<AdminUser>> = _users
    val updateStatus: LiveData<Resource<Unit>> = _updateStatus

    init {
        loadUsers()
    }

    fun loadUsers() {
        firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                _users.value = snapshot?.documents?.mapNotNull { doc ->
                    AdminUser(
                        uid = doc.id,
                        email = doc.getString("email") ?: "",
                        isAdmin = doc.getBoolean("isAdmin") ?: false
                    )
                } ?: emptyList()
            }
    }

    fun updateAdminStatus(userId: String, isAdmin: Boolean) {
        _updateStatus.value = Resource.Loading()
        firestore.collection("users").document(userId)
            .update("isAdmin", isAdmin)
            .addOnCompleteListener { task ->
                _updateStatus.value = if (task.isSuccessful) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(task.exception?.message ?: "Update failed")
                }
            }
    }

}

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}