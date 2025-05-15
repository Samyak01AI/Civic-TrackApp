package com.example.civic_trackapplication.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.civic_trackapplication.Issue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class StatusViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val issuesCollection = firestore.collection("issues")

    private val _filteredIssues = MutableLiveData<List<Issue>>()
    val filteredIssues: LiveData<List<Issue>> = _filteredIssues

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentFilter: String? = null
    private var currentSortField = "timestamp"

    init {
        loadIssues()
    }

    fun setStatusFilter(status: String?) {
        currentFilter = status
        loadIssues()
    }

    fun setSortOrder(field: String) {
        currentSortField = field
        loadIssues()
    }

    fun refreshData() {
        loadIssues()
    }

    private fun loadIssues() {
        _isLoading.value = true

        var query = issuesCollection.orderBy(currentSortField, Query.Direction.DESCENDING)

        currentFilter?.let { status ->
            query = query.whereEqualTo("status", status)
        }

        query.addSnapshotListener { snapshot, error ->
            _isLoading.value = false

            if (error != null) {
                Log.e("StatusFragment", "Error loading issues", error)
                return@addSnapshotListener
            }

            val issues = snapshot?.documents?.mapNotNull { doc ->
                Issue.fromDocument(doc)
            } ?: emptyList()

            _filteredIssues.value = issues
        }
    }
}