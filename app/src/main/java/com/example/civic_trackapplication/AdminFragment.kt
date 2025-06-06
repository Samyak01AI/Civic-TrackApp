package com.example.civic_trackapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.civic_trackapplication.adapter.AdminIssuesAdapter
import com.example.civic_trackapplication.adapter.IssuesAdapter
import com.example.civic_trackapplication.databinding.FragmentAdminBinding
import com.example.civic_trackapplication.viewmodels.AdminStatsViewModel
import com.example.civic_trackapplication.viewmodels.IssuesViewModel
import com.example.civic_trackapplication.viewmodels.Resource
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.snackbar.Snackbar

class AdminFragment : Fragment() {
    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminStatsViewModel by viewModels()

    private val issueviewModel: IssuesViewModel by viewModels()
    private lateinit var issuesAdapter: AdminIssuesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = "Admin Dashboard"

        setupRecyclerView()
        setupObservers()
        loadData()
    }


    private fun setupRecyclerView() {
        issuesAdapter = AdminIssuesAdapter(
            onStatusChanged = { issueId, newStatus ->
                issueviewModel.updateIssueStatus(issueId, newStatus)
            },
            onItemClick = { issue ->
                showIssueDetailsDialog(issue)
            }
        )

        with(binding.issuesRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = issuesAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun setupObservers() {
        // Header stats observer
        viewModel.headerStats.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                binding.tvUsersCount.text = it.userCount.toString()
                binding.tvIssuesCount.text = it.issueCount.toString()
                binding.tvResolvedCount.text = "${it.resolvedPercentage}%"
            } ?: showError("Failed to load header stats")
        }
        issueviewModel.issues.observe(viewLifecycleOwner) { issues ->
            issuesAdapter.submitList(issues)

            if (issues.isEmpty()) {
                showEmptyState()
            }
        }
    }

    private fun loadData() {
        viewModel.loadAllStats()
    }

    private fun showIssueDetailsDialog(issue: Issue) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_issue_details, null)

        dialogView.findViewById<TextView>(R.id.tvTitle).text = issue.title
        dialogView.findViewById<TextView>(R.id.tvLocation).text = issue.location
        dialogView.findViewById<TextView>(R.id.tvDescription).text = issue.description
        dialogView.findViewById<TextView>(R.id.tvStatus).text = issue.status
        Glide.with(this)
            .load(issue.imageUrl)
            .into(dialogView.findViewById<ImageView>(R.id.ivIssueImage))

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Issue Details")
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showEmptyState() {
        binding.issuesRecyclerView.visibility = View.GONE
    }


    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}