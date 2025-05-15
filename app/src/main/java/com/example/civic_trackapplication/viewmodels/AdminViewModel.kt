package com.example.civic_trackapplication.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.civic_trackapplication.Issue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _issues = MutableLiveData<List<Issue>>()
    val issues: LiveData<List<Issue>> = _issues

    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> = _isAdmin

    init {
        checkAdminStatus()
        loadIssues()
    }

    private fun checkAdminStatus() {
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    _isAdmin.value = doc.getBoolean("isAdmin") ?: false
                }
        }
    }

    fun updateIssueStatus(issueId: String, newStatus: String) {
        firestore.collection("issues").document(issueId)
            .update("status", newStatus)
            .addOnSuccessListener {
                // Status updated
            }
            .addOnFailureListener { e ->
                Log.e("Admin", "Error updating status", e)
            }
    }
    private val _currentFilter = MutableLiveData(StatusFilter.ALL)
    val currentFilter: LiveData<StatusFilter> = _currentFilter

    // Add this method
    fun setFilter(filter: StatusFilter) {
        _currentFilter.value = filter
        loadIssues(filter)
    }

    // Modify loadIssues to accept filter
    private fun loadIssues(filter: StatusFilter = StatusFilter.ALL) {
        var query = firestore.collection("issues")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        if (filter != StatusFilter.ALL) {
            query = query.whereEqualTo("status", filter.name.lowercase())
        }

        query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("Admin", "Error loading issues", error)
                return@addSnapshotListener
            }
            _issues.value = snapshot?.toObjects(Issue::class.java) ?: emptyList()
        }
    }

    enum class StatusFilter { ALL, PENDING, IN_PROGRESS, RESOLVED }
}