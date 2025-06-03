package com.example.civic_trackapplication

import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.bumptech.glide.Glide
import com.example.civic_trackapplication.adapter.IssuesAdapter
import com.example.civic_trackapplication.adapter.StatusIssuesAdapter
import com.example.civic_trackapplication.databinding.FragmentStatusBinding
import com.example.civic_trackapplication.viewmodels.StatusViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.logging.Handler

class StatusFragment : Fragment() {
    private var _binding: FragmentStatusBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatusViewModel by viewModels()
    private lateinit var issuesAdapter: StatusIssuesAdapter
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterChips()
        setupCharts()
        observeIssues()
        setupSwipeRefresh()
        observeChartData()

        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedChip = group.findViewById<Chip>(checkedId)
            val status = selectedChip?.text?.toString()

            if (status != null) {
                filterIssuesByCategory(status)
            }
        }
    }

    private fun setupRecyclerView() {
        issuesAdapter = StatusIssuesAdapter(
            onItemClick = { issue ->
                showIssueDetailsDialog(issue)
            }
        )

        binding.rvIssues.apply {
            adapter = issuesAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
    }
    private fun setupCharts() {
        pieChart = binding.pieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            legend.isEnabled = true
            layoutParams.width = 1000
            layoutParams.height = 1000
            isRotationEnabled = false
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(9f)
            animateY(1000, Easing.EaseInOutQuad)

            setDrawEntryLabels(true)
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)
            setDrawCenterText(false)
            setExtraOffsets(20f, 0f, 20f, 20f)
        }
        barChart = binding.barChart.apply {
            setDrawValueAboveBar(true)
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            animateY(1000)
        }
    }

    private fun observeChartData() {
        viewModel.issuesForCharts.observe(viewLifecycleOwner) { issues ->
            updatePieChart(issues)
            updateBarChart(issues)
        }
    }

  private fun updatePieChart(issues: List<Issue>) {
        // Debug: Print all received issues
        Log.d("ChartDebug", "Received ${issues.size} issues")
        issues.forEach { issue ->
            Log.d("ChartDebug", "Issue ID: ${issue.id}, Status: ${issue.status}")
        }

        // Group and count statuses (case-insensitive)
        val statusCount = issues
            .filter { !it.status.isNullOrEmpty() }
            .groupingBy { it.status!!.lowercase().trim() }
            .eachCount()

        Log.d("ChartDebug", "Status counts: $statusCount")

        val entries = listOf(
            PieEntry(statusCount["pending"]?.toFloat() ?: 0f, "Pending"),
            PieEntry(statusCount["in progress"]?.toFloat() ?: 0f, "In Progress"),
            PieEntry(statusCount["resolved"]?.toFloat() ?: 0f, "Resolved")
        ).filter { it.value > 0f }

        if (entries.isEmpty()) {
            Log.w("ChartDebug", "No valid data to display in pie chart")
            binding.pieChart.clear()
            binding.pieChart.invalidate()
            return
        }

        Log.d("ChartDebug", "Chart entries: $entries")

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.status_pending),
                ContextCompat.getColor(requireContext(), R.color.status_processing),
                ContextCompat.getColor(requireContext(), R.color.status_approved)
            )
            valueTextSize = 14f
            valueTextColor = Color.BLACK
        }

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.invalidate()
        binding.pieChart.animateY(1000)
    }

    private fun updateBarChart(issues: List<Issue>) {
        val dailyCount = issues.groupBy {
            SimpleDateFormat("dd MMM", Locale.getDefault()).format(it.timestamp)
        }.mapValues { it.value.size }

        val entries = dailyCount.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }

        val xAxisLabels = dailyCount.keys.toList()

        val dataSet = BarDataSet(entries, "Issues per Day").apply {
            color = ContextCompat.getColor(requireContext(), R.color.status_processing)
            valueTextSize = 10f
        }

        barChart.apply {
            data = BarData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
            invalidate()
        }
    }
    private fun setupFilterChips() {
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedStatus = when (checkedIds.firstOrNull()) {
                R.id.chip_all -> StatusViewModel.StatusFilter.ALL
                R.id.chip_pending -> StatusViewModel.StatusFilter.PENDING
                R.id.chip_in_progress -> StatusViewModel.StatusFilter.IN_PROGRESS
                R.id.chip_resolved -> StatusViewModel.StatusFilter.RESOLVED
                else -> StatusViewModel.StatusFilter.ALL
            }
            viewModel.setFilter(selectedStatus)
        }
    }

    private fun observeIssues() {
        viewModel.filteredIssues.observe(viewLifecycleOwner) { issues ->
            issuesAdapter.submitList(issues)
            binding.emptyState.root.visibility = if (issues.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshIssues()
        }
    }

    private fun filterIssuesByCategory(status: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Issues")
            .whereEqualTo("status", status)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val filteredIssues = querySnapshot.toObjects(Issue::class.java)
                issuesAdapter.submitList(filteredIssues)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
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

        AlertDialog.Builder(requireContext())
            .setTitle("Issue Details")
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}