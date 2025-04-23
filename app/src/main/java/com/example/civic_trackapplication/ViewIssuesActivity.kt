package com.example.civic_trackapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.civic_trackapplication.R
import com.example.civic_trackapplication.IssueAdapter

class ViewIssuesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_issues)

        // Dummy data for testing
        val dummyIssues = listOf("Pothole", "Garbage", "Water Leakage")

        // Setup RecyclerView
        val rvIssues = findViewById<RecyclerView>(R.id.rvIssues)
        rvIssues.layoutManager = LinearLayoutManager(this)
        rvIssues.adapter = IssueAdapter(dummyIssues) // Create this adapter
    }
}