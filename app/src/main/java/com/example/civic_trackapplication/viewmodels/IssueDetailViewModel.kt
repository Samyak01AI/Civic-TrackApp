package com.example.civic_trackapplication.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.civic_trackapplication.Issue
import com.google.firebase.firestore.FirebaseFirestore

class IssueDetailViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _issue = MutableLiveData<Issue?>()
    val issue: LiveData<Issue?> = _issue

    fun loadIssueDetails(issueId: String) {
        firestore.collection("issues").document(issueId)
            .get()
            .addOnSuccessListener { document ->
                _issue.value = document.toObject(Issue::class.java)?.copy(id = document.id)
            }
            .addOnFailureListener { e ->
                Log.e("IssueDetail", "Error loading issue", e)
                _issue.value = null
            }
    }
}