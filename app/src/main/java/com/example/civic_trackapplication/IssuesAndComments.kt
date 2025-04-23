package com.example.civic_trackapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore

class IssuesAndComments {

    fun createOrCommentOnIssue(
        issueId: String,
        title: String,
        description: String,
        category: String = "General",
        status: String = "Open",
        userComment: String,
        submittedBy: String = Firebase.auth.currentUser?.uid ?: "anonymous"
    ) {
        val db = Firebase.firestore
        val issueRef = db.collection("Issues").document()
        val userId = Firebase.auth.currentUser?.uid ?: "anonymous"

        val comment = hashMapOf(
            "userId" to userId,
            "text" to userComment,
            "time" to Timestamp.now()
        )

        issueRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                // Add comment to existing issue
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(issueRef)
                    val existing = snapshot.get("comments")

                    val commentsList = if (existing is List<*>) {
                        existing.filterIsInstance<Map<String, Any>>().toMutableList()
                    } else {
                        mutableListOf()
                    }
                    commentsList.add(comment)
                    transaction.update(issueRef, "comments", commentsList)
                }.addOnSuccessListener {
                    println("Comment added to existing issue.")
                }.addOnFailureListener {
                    println("Failed to add comment: ${it.message}")
                }

            } else {
                // Create issue and add comment
                val newIssue = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "category" to category,
                    "upvotes" to 0L,
                    "status" to status,
                    "comments" to listOf(comment),
                    "submittedBy" to submittedBy
                )

                issueRef.set(newIssue).addOnSuccessListener {
                    println("New issue created and comment added.")
                }.addOnFailureListener {
                    println("Failed to create issue: ${it.message}")
                }
            }
        }.addOnFailureListener {
            println("Failed to check issue existence: ${it.message}")
        }
    }
}