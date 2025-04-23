package com.example.civic_trackapplication

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.messaging
import com.google.firebase.messaging.remoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
            message.notification?.let {
                // Show custom notification if needed
                Log.d("FCM", "Message: ${it.title} - ${it.body}")
            }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Firebase.messaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val userId = Firebase.auth.currentUser?.uid

                // Save token to Firestore
                Firebase.firestore.collection("users").document(token ?: "unknown")
                    .update("fcmToken", token)
            }
        }
    }
    
}