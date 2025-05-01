package com.example.civic_trackapplication

import android.os.Bundle
import android.view.View
import com.google.android.material.chip.ChipGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class ViewIssuesActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: IssueAdapter
    private lateinit var chipGroup: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_issues)

        recycler = findViewById(R.id.recyclerIssues)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = IssueAdapter(emptyList())
        recycler.adapter = adapter

        chipGroup = findViewById(R.id.chipGroup)
        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chipText = findViewById<View>(checkedId)?.let { chip ->
                (chip as? Chip)?.text.toString()
            } ?: "All"
            adapter.filterByCategory(chipText)
        }

        adapter.filterByCategory("All") // show all by default
    }
}
