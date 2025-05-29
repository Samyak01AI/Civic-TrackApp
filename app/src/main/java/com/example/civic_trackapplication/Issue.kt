package com.example.civic_trackapplication

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Issue(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val status: String = "",
    val priorityScore: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String = "",
    val userId: String = "",
    val userName: String = ""
){
    companion object {
        fun fromDocument(document: DocumentSnapshot): Issue? {
                return Issue(
                    id = document.id,
                    title = document.getString("title") ?: "",
                    description = document.getString("description") ?: "",
                    location = document.getString("location") ?: "",
                    timestamp = document.getTimestamp("timestamp")?.toDate()?.time ?: System.currentTimeMillis(),
                    status = document.getString("status") ?: "",
                    imageUrl = document.getString("imageUrl") ?: "",
                    priorityScore = document.getDouble("priority_score")?.toInt() ?: 0,
                    userName = document.getString("userName") ?: "",
                    userId = document.getString("userId") ?: "",
                )
        }
        fun formatTimestampToMonthDate(timestamp: Long): String {
            val date = Date(timestamp)
            val sdf = SimpleDateFormat("dd MMM yy", Locale.getDefault())
            return sdf.format(date)
        }
    }
}