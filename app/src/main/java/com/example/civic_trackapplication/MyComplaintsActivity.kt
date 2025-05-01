package com.example.civic_trackapplication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyComplaintsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_complaints)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerComplaints)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Dummy data (replace with real data from Firebase or DB)
        val complaints = listOf("Pothole on 5th Ave", "Garbage not collected", "Streetlight broken")
        recyclerView.adapter = SimpleListAdapter(complaints)
    }
}