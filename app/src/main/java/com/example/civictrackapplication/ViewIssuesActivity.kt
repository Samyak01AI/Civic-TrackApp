package com.example.civictrackapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.civic_trackapplication.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewIssuesActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: IssueAdapter
    private lateinit var chipGroup: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_issues)


        recycler = findViewById(R.id.recyclerIssues)
        chipGroup = findViewById(R.id.chipGroup)

        recycler.layoutManager = LinearLayoutManager(this)

        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("Issues")
                .whereEqualTo("submittedBy", userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    val issues = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Issue::class.java)
                        } catch (e: Exception) {
                            Log.e("IssueParseError", "Error parsing issue", e)
                            null
                        }
                    }

                    adapter = IssueAdapter(issues)
                    recycler.adapter = adapter


                    chipGroup.setOnCheckedChangeListener { group, checkedId ->
                        val chipText = findViewById<View>(checkedId)?.let { chip ->
                            (chip as? Chip)?.text.toString()
                        } ?: "All"
                        adapter.filterByCategory(chipText)
                    }

                    // Show all issues by default
                    adapter.filterByCategory("All")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load issues: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("FirestoreError", "Failed to load issues", e)
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
