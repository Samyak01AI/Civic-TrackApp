package com.example.civic_trackapplication
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.civic_trackapplication.R

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Optional: Get the user's name from intent if passed
        val username = intent.getStringExtra("username") ?: "User"
        Toast.makeText(this, "Welcome $username 👋", Toast.LENGTH_SHORT).show()

        // Set up button click listeners
        findViewById<LinearLayout>(R.id.cardReportIssue).setOnClickListener {
            startActivity(Intent(this, ReportIssueActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardViewIssues).setOnClickListener {
            startActivity(Intent(this, ViewIssuesActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardMyComplaints).setOnClickListener {
            startActivity(Intent(this, MyComplaintsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardAccount).setOnClickListener {
            startActivity(Intent(this, MyAccountActivity::class.java))
        }
    }
}
