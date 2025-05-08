package com.example.civic_trackapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.chip.ChipGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewIssuesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var issueAdapter: IssueAdapter
    private val issueList = mutableListOf<Issue>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_issues)

        recyclerView = findViewById(R.id.recyclerIssues)
        recyclerView.layoutManager = LinearLayoutManager(this)
        issueAdapter = IssueAdapter(mutableListOf())
        recyclerView.adapter = issueAdapter

        fetchIssuesLive()

    }

  /*  private fun fetchIssuesFromFirestore() {
        FirebaseFirestore.getInstance().collection("Issues")
            .get()
            .addOnSuccessListener { result ->
                issueList.clear()
                for (document in result) {
                    val issue = document.toObject(Issue::class.java)
                    issueList.add(issue)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load issues: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }*/

    private fun fetchIssuesLive() {
        FirebaseFirestore.getInstance()
            .collection("Issues")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Listen failed: ", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val issueList = mutableListOf<Issue>()
                    for (doc in snapshot) {
                        val issue = doc.toObject(Issue::class.java)
                        issueList.add(issue)
                    }
                    issueAdapter.updateData(issueList)
                }
            }
    }

}