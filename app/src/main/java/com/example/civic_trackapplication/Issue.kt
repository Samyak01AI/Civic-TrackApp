package com.example.civic_trackapplication

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint

data class Issue(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "pending", // pending/in-progress/resolved
    val imageUrl: String = "",
    val userId: String = ""
) {
    companion object {
        fun fromDocument(document: DocumentSnapshot): Issue {
            return document.toObject(Issue::class.java)!!.copy(id = document.id)
        }
    }
}