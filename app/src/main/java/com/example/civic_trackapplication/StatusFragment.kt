package com.example.civic_trackapplication

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
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
import java.text.SimpleDateFormat
import java.util.Locale

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
        observeChartData()
        observeIssues()
        setupSwipeRefresh()
    }

    private fun setupRecyclerView() {
        issuesAdapter = StatusIssuesAdapter { issue ->
            navigateToIssueDetail(issue.id)
        }

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
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            animateY(1000, Easing.EaseInOutQuad)
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
        val statusCount = issues.groupingBy { it.status }.eachCount()

        val entries = listOf(
            PieEntry(statusCount["pending"]?.toFloat() ?: 0f, "Pending"),
            PieEntry(statusCount["in_progress"]?.toFloat() ?: 0f, "In Progress"),
            PieEntry(statusCount["resolved"]?.toFloat() ?: 0f, "Resolved")
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.status_pending),
                ContextCompat.getColor(requireContext(), R.color.status_in_progress),
                ContextCompat.getColor(requireContext(), R.color.status_resolved)
            )
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        pieChart.data = PieData(dataSet)
        pieChart.invalidate()
    }

    private fun updateBarChart(issues: List<Issue>) {
        val dailyCount = issues.groupBy {
            SimpleDateFormat("dd MMM", Locale.getDefault()).format(it.timestamp.toDate())
        }.mapValues { it.value.size }

        val entries = dailyCount.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }

        val xAxisLabels = dailyCount.keys.toList()

        val dataSet = BarDataSet(entries, "Issues per Day").apply {
            color = ContextCompat.getColor(requireContext(), R.color.m3_primary_container)
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

    private fun navigateToIssueDetail(issueId: String) {
        findNavController().navigate(
            StatusFragmentDirections.actionStatusFragmentToIssueDetailFragment(issueId)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}