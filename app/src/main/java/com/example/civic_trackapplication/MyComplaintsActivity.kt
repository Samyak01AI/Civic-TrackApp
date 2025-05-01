package com.example.civic_trackapplication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyComplaintsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: IssueAdapter  // Reuse your adapter
    private lateinit var db: FirebaseFirestore
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_complaints)

        recyclerView = findViewById(R.id.recyclerComplaints)
        recyclerView.layoutManager = LinearLayoutManager(this)

        db = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        loadUserComplaints()
    }

    private fun loadUserComplaints() {
        db.collection("Issues")
            .whereEqualTo("submittedBy", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val issues = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Issue::class.java)
                }

                adapter = IssueAdapter(issues)
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load complaints", Toast.LENGTH_SHORT).show()
                Log.e("MyComplaints", "Error: ${e.message}")
            }
    }
}
