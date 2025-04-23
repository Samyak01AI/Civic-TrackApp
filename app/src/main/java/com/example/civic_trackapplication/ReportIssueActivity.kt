package com.example.civic_trackapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ReportIssueActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_issue)

        // Initialize UI elements
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        // TODO: Add CameraX integration
        // TODO: Add location fetching
        // TODO: Connect to Firestore

        btnSubmit.setOnClickListener {
            // Temporary action
            finish() // Close the screen for now
        }
    }
}