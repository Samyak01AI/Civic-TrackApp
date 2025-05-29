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
    private val _issuesForCharts = MutableLiveData<List<Issue>>()
    val issuesForCharts: LiveData<List<Issue>> = _issuesForCharts
    private val _filteredIssues = MutableLiveData<List<Issue>>()
    val filteredIssues: LiveData<List<Issue>> = _filteredIssues

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val currentFilter = MutableLiveData(StatusFilter.ALL)

    init {
        currentFilter.observeForever { filter ->
            loadIssues(filter)
        }
    }

    fun setFilter(filter: StatusFilter) {
        currentFilter.value = filter
    }

    fun refreshIssues() {
        currentFilter.value?.let { loadIssues(it) }
    }


    private fun loadIssues(filter: StatusFilter) {
        _isLoading.value = true

        var query = firestore.collection("Issues")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        if (filter != StatusFilter.ALL) {
            query = query.whereEqualTo("status", filter.name.lowercase())
        }

        query.addSnapshotListener { snapshot, error ->
            _isLoading.value = false
            if (error != null) return@addSnapshotListener

            val issues = snapshot?.documents?.mapNotNull { doc ->
                Issue.fromDocument(doc)
            } ?: emptyList()

            _filteredIssues.value = issues
            _issuesForCharts.value = issues.takeLast(30) // Last 30 for charts
        }
    }
    fun loadIssues() {
        firestore.collection("Issues").get()
            .addOnSuccessListener { documents ->
                val issues = documents.toObjects(Issue::class.java)
                _issuesForCharts.value = issues // Make sure you're posting to LiveData
                Log.d("ViewModel", "Loaded ${issues.size} issues")
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "Error loading issues", e)
            }
    }

    enum class StatusFilter { ALL, PENDING, IN_PROGRESS, RESOLVED }
}