package com.example.civic_trackapplication.repository

import com.example.civic_trackapplication.Issue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class IssuesRepository {
    private val db = FirebaseFirestore.getInstance()
    private val issuesCollection = db.collection("Issues")

    // Get all issues
    suspend fun getIssues(): List<Issue> {
        return try {
            issuesCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Issue::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Update issue status
    suspend fun updateIssueStatus(issueId: String, newStatus: String): Boolean {
        return try {
            issuesCollection.document(issueId)
                .update("status", newStatus)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}