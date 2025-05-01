package com.example.civic_trackapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

            val user = FirebaseAuth.getInstance().currentUser
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
            Intent(this, MyComplaintsActivity::class.java).also {
                startActivity(it)
            }
        }
        val cardAccount = findViewById<LinearLayout>(R.id.cardAccount)
        cardAccount.setOnClickListener {
            // Handle my complaints button click
            Intent(this, MyAccountActivity::class.java).also {
                startActivity(it)
            }
        }
    }
}