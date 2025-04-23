package com.example.civic_trackapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.civic_trackapplication.R

class MyComplaintsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_complaints)

        // Dummy data for testing
        val dummyComplaints = listOf("Complaint 1", "Complaint 2", "Complaint 3")

        // Setup RecyclerView
        val rvComplaints = findViewById<RecyclerView>(R.id.rvComplaints)
        rvComplaints.layoutManager = LinearLayoutManager(this)
        rvComplaints.adapter = ComplaintAdapter(dummyComplaints) // Create this adapter
    }
}