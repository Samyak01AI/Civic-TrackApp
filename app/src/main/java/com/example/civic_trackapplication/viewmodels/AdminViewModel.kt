package com.example.civic_trackapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.civic_trackapplication.Issue
import com.example.civic_trackapplication.repository.IssuesRepository
import com.example.civic_trackapplication.viewmodels.AdminStats
import com.example.civic_trackapplication.viewmodels.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val repository = IssuesRepository()

    // LiveData for UI observation
    private val _issues = MutableLiveData<List<Issue>>()
    val issues: LiveData<List<Issue>> = _issues

    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> = _isAdmin

    private val _stats = MutableLiveData<AdminStats>()
    val stats: LiveData<AdminStats> = _stats

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        checkAdminStatus()
        loadAllData()
    }

    private fun checkAdminStatus() {
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    _isAdmin.value = doc.getBoolean("isAdmin") ?: false
                }
                .addOnFailureListener { e ->
                    _error.value = "Failed to check admin status: ${e.message}"
                    Log.e("Admin", "Error checking admin status", e)
                }
        } ?: run {
            _error.value = "No authenticated user found"
        }
    }

    fun loadAllData() {
        _isLoading.value = true
        loadIssues()
        loadStats()
    }

    fun loadIssues(filter: StatusFilter = StatusFilter.ALL) {
        _isLoading.value = true

        var query = firestore.collection("Issues")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        if (filter != StatusFilter.ALL) {
            query = query.whereEqualTo("status", filter.name.lowercase())
        }

        query.addSnapshotListener { snapshot, error ->
            _isLoading.value = false

            error?.let {
                _error.value = "Error loading issues: ${it.message}"
                Log.e("Admin", "Error loading issues", it)
                return@addSnapshotListener
            }

            _issues.value = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject<Issue>()?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e("Admin", "Error parsing issue ${doc.id}", e)
                    null
                }
            } ?: emptyList()
        }
    }

    fun loadStats() {
        _isLoading.value = true

        firestore.collection("users")
            .get()
            .addOnSuccessListener { usersSnapshot ->
                val userCount = usersSnapshot.size()

                firestore.collection("Issues")
                    .get()
                    .addOnSuccessListener { issuesSnapshot ->
                        val totalIssues = issuesSnapshot.size()
                        val resolvedIssues = issuesSnapshot.count {
                            it.getString("status")?.equals("resolved", ignoreCase = true) == true
                        }

                        val resolvedPercentage = if (totalIssues > 0) {
                            (resolvedIssues * 100 / totalIssues)
                        } else 0

                        _stats.value = AdminStats(
                            userCount = userCount,
                            issueCount = totalIssues,
                            resolvedPercentage = resolvedPercentage,
                            statusDistribution = mapOf(
                                "Pending" to issuesSnapshot.count {
                                    it.getString("status")?.equals("pending", ignoreCase = true) == true
                                },
                                "In Progress" to issuesSnapshot.count {
                                    it.getString("status")?.equals("in progress", ignoreCase = true) == true
                                },
                                "Resolved" to resolvedIssues
                            ),
                            weeklyTrends = emptyMap()
                        )
                        _isLoading.value = false
                    }
                    .addOnFailureListener { e ->
                        _error.value = "Failed to load issues: ${e.message}"
                        _isLoading.value = false
                        Log.e("Admin", "Error loading issues for stats", e)
                    }
            }
            .addOnFailureListener { e ->
                _error.value = "Failed to load users: ${e.message}"
                _isLoading.value = false
                Log.e("Admin", "Error loading users for stats", e)
            }
    }

    fun clearError() {
        _error.value = null
    }
    fun updateIssueStatus(issueId: String, newStatus: String): LiveData<Resource<Unit>> {
        val result = MutableLiveData<Resource<Unit>>()
        viewModelScope.launch {
            try {
                repository.updateIssueStatus(issueId, newStatus)
                result.postValue(Resource.Success(Unit))
            } catch (e: Exception) {
                result.postValue(Resource.Error(e.message ?: "Update failed"))
            }
        }
        return result
    }

    enum class StatusFilter { ALL, PENDING, IN_PROGRESS, RESOLVED }
}

