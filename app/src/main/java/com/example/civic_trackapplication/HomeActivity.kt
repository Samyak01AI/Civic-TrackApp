package com.example.civic_trackapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val user = FirebaseAuth.getInstance().getCurrentUser()
        if (user != null) {
            val name = user.getDisplayName().toString()
            val welcomeText = findViewById<TextView>(R.id.welcomeText)
            welcomeText.text = "Hi, $name 👋"
        }

        val btnReportIssue = findViewById<Button>(R.id.btnReportIssue)
        btnReportIssue.setOnClickListener {
            // Handle report issue button click
            Intent(this, ReportIssueActivity::class.java).also {
                startActivity(it)
            }
        }
        val btnViewIssues = findViewById<Button>(R.id.btnViewIssues)
        btnViewIssues.setOnClickListener {
            // Handle view issues button click
            Intent(this, ViewIssuesActivity::class.java).also {
                startActivity(it)
            }
        }
        val btnMyComplaints = findViewById<Button>(R.id.btnMyComplaints)
        btnMyComplaints.setOnClickListener {
            // Handle my complaints button click
            Intent(this, MyComplaintsActivity::class.java).also {
                startActivity(it)
            }
        }
    }
}