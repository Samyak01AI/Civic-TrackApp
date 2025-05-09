package com.example.civictrackapplication

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class IssueViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val _filteredIssues = MutableLiveData<List<Map<String, Any>>>()
    val filteredIssues: LiveData<List<Map<String, Any>>> = _filteredIssues

    /*  fun fetchIssuesByUser(userId: String) {
          db.collection("issues")
              .whereEqualTo("submittedBy", userId)
              .get()
              .addOnSuccessListener { documents ->
                  val issues = documents.map { it.data }
                  _filteredIssues.value = issues
              }
              .addOnFailureListener {
                  // handle error, maybe log or show a message
                  _filteredIssues.value = emptyList()
              }
      }*/

    fun fetchIssuesByUser(userId: String) {
        val db = Firebase.firestore
        Log.d("FirestoreIssues", "Issue ID: "+userId)
        db.collection("issues")
            .whereEqualTo("submittedBy", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("FirestoreIssues", "Issue ID: ${document.id}, Data: ${document.data}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreIssues", "Error fetching issues: ${e.message}", e)
            }
    }


    fun fetchIssuesByCategory(category: String) {
        db.collection("issues")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { documents ->
                val issues = documents.map { it.data }
                _filteredIssues.value = issues
            }
            .addOnFailureListener {
                _filteredIssues.value = emptyList()
            }
    }

    fun fetchIssuesByUserAndCategory(userId: String, category: String) {
        db.collection("issues")
            .whereEqualTo("submittedBy", userId)
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { documents ->
                val issues = documents.map { it.data }
                _filteredIssues.value = issues
            }
            .addOnFailureListener {
                _filteredIssues.value = emptyList()
            }
    }
}