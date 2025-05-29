package com.example.civic_trackapplication.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.civic_trackapplication.Issue
import com.example.civic_trackapplication.repository.IssuesRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

class IssuesViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val repository = IssuesRepository()
    private val issuesCollection = firestore.collection("Issues")

    private val _issues = MutableLiveData<List<Issue>>()
    val issues: LiveData<List<Issue>> = _issues

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadIssues()
    }

    fun refreshIssues() {
        loadIssues()
    }

    private fun loadIssues() {
        _isLoading.value = true

        issuesCollection
            .orderBy("priority_score", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false

                if (error != null) {
                    Log.e("HomeFragment", "Error fetching issues", error)
                    return@addSnapshotListener
                }

                val issuesList = snapshot?.documents?.map { doc ->
                    Issue.fromDocument(doc)
                } ?: emptyList()

                _issues.value = issuesList as List<Issue>?
            }
    }
    fun updateIssueStatus(issueId: String, newStatus: String) {
        viewModelScope.launch {
            val success = repository.updateIssueStatus(issueId, newStatus)
            if (!success) {
                // Handle error if needed
            }
        }
    }
}