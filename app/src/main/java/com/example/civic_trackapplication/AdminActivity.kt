package com.example.civic_trackapplication

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.civic_trackapplication.adapter.AdminIssuesAdapter
import com.example.civic_trackapplication.databinding.ActivityAdminBinding
import com.example.civic_trackapplication.viewmodels.AdminViewModel

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var issuesAdapter: AdminIssuesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeIssues()
        setupFilter()
    }

    private fun setupRecyclerView() {
        issuesAdapter = AdminIssuesAdapter { issue, newStatus ->
            viewModel.updateIssueStatus(issue.id, newStatus)
        }

        binding.rvIssues.apply {
            adapter = issuesAdapter
            layoutManager = LinearLayoutManager(this@AdminActivity)
            addItemDecoration(
                DividerItemDecoration(
                    this@AdminActivity,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    private fun observeIssues() {
        viewModel.issues.observe(this) { issues ->
            issuesAdapter.submitList(issues)
            binding.emptyState.root.visibility = if (issues.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isAdmin.observe(this) { isAdmin ->
            if (!isAdmin) {
                Toast.makeText(this, "Admin access required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupFilter() {
        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                viewModel.setFilter(AdminViewModel.StatusFilter.entries[pos])
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }
}