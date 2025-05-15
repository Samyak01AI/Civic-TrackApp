package com.example.civic_trackapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging


class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        askNotificationPermission()

        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        val name = user?.displayName
        val email = user?.email
        val photoUrl = user?.photoUrl?.toString()


        if (uid != null) {
            val userMap = hashMapOf(
                "name" to name,
                "email" to email,
                "photoUrl" to photoUrl
            )

            FirebaseFirestore.getInstance().collection("Users")
                .document(uid)
                .set(userMap)
                .addOnSuccessListener {
                    Log.d("Firestore", "User profile stored successfully.")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error saving user profile", e)
                }
        }


            if (user != null) {
                val name = user.displayName
                Toast.makeText(this, "Welcome, $name!", Toast.LENGTH_SHORT).show()
            }
        val cardReportIssue = findViewById<LinearLayout>(R.id.cardReportIssue)
        cardReportIssue.setOnClickListener {
            // Handle report issue button click
            Intent(this, ReportIssueActivity::class.java).also {
                startActivity(it)
            }
        }
        val cardViewIssues = findViewById<LinearLayout>(R.id.cardViewIssues)
        cardViewIssues.setOnClickListener {
            // Handle view issues button click
            Intent(this, ViewIssuesActivity::class.java).also {
                startActivity(it)
            }
        }
        val cardMyComplaints = findViewById<LinearLayout>(R.id.cardMyComplaints)
        cardMyComplaints.setOnClickListener {
            // Handle my complaints button click
           /* Intent(this, MyComplaintsActivity::class.java).also {
                startActivity(it)
            }*/
        }
        val cardAccount = findViewById<LinearLayout>(R.id.cardAccount)
        cardAccount.setOnClickListener {
            // Handle my complaints button click
            Intent(this, MyAccountActivity::class.java).also {
                startActivity(it)
            }
        }

        FirebaseMessaging.getInstance().subscribeToTopic("issues")
            .addOnCompleteListener(OnCompleteListener { task: Task<Void?>? ->
                var msg = "Subscribed to topic"
                if (!task!!.isSuccessful()) {
                    msg = "Subscription failed"
                }
                Log.d("FCM", msg)
            })

    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("NotificationPermission", "Notification permission granted")
            // You can now receive notifications
        } else {
            Log.d("NotificationPermission", "Notification permission denied")
            // Inform user that permission is required
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

}