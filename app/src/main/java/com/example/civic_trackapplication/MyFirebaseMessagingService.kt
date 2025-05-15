package com.example.civic_trackapplication

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = if (remoteMessage.getNotification() != null) remoteMessage.getNotification()!!
            .getTitle() else "No Title"
        val body = if (remoteMessage.getNotification() != null) remoteMessage.getNotification()!!
            .getBody() else "No Body"

        sendNotification(title, body)
    }

    private fun sendNotification(title: String?, message: String?) {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "default")
            .setSmallIcon(R.drawable.ic_notification_overlay)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default", "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        manager.notify(0, builder.build())
    }
}

