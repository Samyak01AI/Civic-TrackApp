package com.example.civic_trackapplication.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.civic_trackapplication.Issue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.groupingBy

class AdminStatsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _statsData = MutableLiveData<AdminStats?>()
    val statsData: LiveData<AdminStats?> = _statsData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _headerStats = MutableLiveData<HeaderStats>()
    val headerStats: LiveData<HeaderStats> = _headerStats

    init {
        _isLoading.value = true
        loadAllStats()
    }

    fun loadAllStats() {
        loadHeaderStats()  // New method for user counts
        loadIssueStats()   // Existing issue analysis
    }

    private fun loadHeaderStats() {
        var issueCount = 0
        firestore.collection("Issues")
            .get()
            .addOnSuccessListener {
                issueCount = it.size()
            }
        // Fetch user count
        firestore.collection("users")
            .get()
            .addOnSuccessListener { usersSnapshot ->
                val userCount = usersSnapshot.size()
                calculateResolvedPercentage { percentage ->
                    Log.d("ResolvedIssues", "Resolved: $percentage%")
                    _headerStats.value = HeaderStats(userCount, issueCount, percentage.toInt())
                    _isLoading.value = false
                }
            }

    }

    fun calculateResolvedPercentage(onResult: (Float) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Issues")
            .get()
            .addOnSuccessListener { result ->
                var total = 0
                var resolved = 0

                for (document in result) {
                    total++
                    val status = document.getString("status")?.lowercase()
                    if (status == "resolved") {
                        resolved++
                    }
                }

                val percentage = if (total > 0) {
                    (resolved.toFloat() / total.toFloat()) * 100
                } else {
                    0f
                }

                _headerStats.value = HeaderStats(
                    userCount = 0,
                    issueCount = total,
                    resolvedPercentage = percentage.toInt()
                )
                onResult(percentage)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                onResult(0f)
            }
    }

    private fun loadIssueStats() {
        firestore.collection("Issues")
            .get()
            .addOnSuccessListener { snapshot ->
                val issues = snapshot.documents.mapNotNull { Issue.fromDocument(it) }
                _statsData.value = processStats(issues)
            }
            .addOnFailureListener { e ->
                Log.e("AdminStats", "Error loading stats", e)
                _statsData.value = null
            }
    }

    private fun processStats(issues: List<Issue>): AdminStats {
        val statusDistribution = issues
            .groupingBy { it.status }
            .eachCount()
            .mapKeys { it.key.replaceFirstChar { char -> char.uppercase() } }

        val weeklyTrends = issues
            .groupBy { issue ->
                SimpleDateFormat("MMM dd", Locale.getDefault())
                    .format(issue.timestamp)
            }
            .mapValues { it.value.size }
            .toSortedMap()

        return AdminStats(
            statusDistribution = statusDistribution,
            weeklyTrends = weeklyTrends,
            userCount = _headerStats.value?.userCount ?: 0,
            issueCount = issues.size,
            resolvedPercentage = _headerStats.value?.resolvedPercentage ?: 0
        )
    }
}

data class AdminStats(
    val statusDistribution: Map<String, Int>,
    val weeklyTrends: Map<String, Int>,
    val userCount: Int,
    val issueCount: Int,
    val resolvedPercentage: Int
) {
    override fun toString(): String {
        return "Users: $userCount, Issues: $issueCount, Resolved: $resolvedPercentage%"
    }
}

data class HeaderStats(
    val userCount: Int,
    val issueCount: Int,
    val resolvedPercentage: Int
)