package com.example.civic_trackapplication

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.civic_trackapplication.databinding.FragmentAdminStatsBinding
import com.example.civic_trackapplication.viewmodels.AdminStatsViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Locale

class AdminStatsFragment : Fragment() {
    private var _binding: FragmentAdminStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminStatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener {
            loadData()
            binding.swipeRefresh.isRefreshing = false
        }
        setupPieChart()
        setupBarChart()
        setupBarChart()
        setupBarChart()
        setupObservers()
        loadData()
        (activity as AppCompatActivity).supportActionBar?.title = "Statistics"
    }

    private fun setupPieChart() {
        val entries = listOf(
            PieEntry(40f, "Resolved"),
            PieEntry(30f, "Pending"),
            PieEntry(20f, "In Progress"),
            PieEntry(10f, "Rejected")
        )

        val dataSet = PieDataSet(entries, "Issue Status")
        dataSet.colors = listOf(
            Color.parseColor("#4CAF50"),  // Green for Resolved
            Color.parseColor("#FFC107"),  // Amber for Pending
            Color.parseColor("#2196F3"),  // Blue for In Progress
            Color.parseColor("#F44336")   // Red for Rejected
        )
        dataSet.sliceSpace = 3f
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        val data = PieData(dataSet)
        binding.pieChart.data = data

        // Customize pie chart appearance
        binding.pieChart.isDrawHoleEnabled = true
        binding.pieChart.holeRadius = 35f
        binding.pieChart.setEntryLabelColor(Color.BLACK)
        binding.pieChart.layoutParams.height = 950
        binding.pieChart.setEntryLabelTextSize(12f)

        // Disable description text
        binding.pieChart.description = Description().apply { text = "" }

        binding.pieChart.animateY(1000)
        binding.pieChart.invalidate() // refresh
    }

    private fun setupBarChart() {
        val entries = listOf(
            BarEntry(1f, 50f),
            BarEntry(2f, 80f),
            BarEntry(3f, 60f),
            BarEntry(4f, 90f)
        )

        val dataSet = BarDataSet(entries, "Issues per Month")
        dataSet.color = Color.parseColor("#5D5FEF")
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val data = BarData(dataSet)
        binding.barChart.data = data

        // Customize bar chart appearance
        binding.barChart.xAxis.apply {
            granularity = 1f
            setDrawGridLines(false)
            setDrawAxisLine(true)
            textColor = Color.BLACK
            valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(
                listOf("Jan", "Feb", "Mar", "Apr")
            )
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        }

        binding.barChart.axisLeft.apply {
            axisMinimum = 0f
            textColor = Color.BLACK
        }
        binding.barChart.axisRight.isEnabled = false

        binding.barChart.description = Description().apply { text = "" }
        binding.barChart.legend.isEnabled = true
        binding.barChart.layoutParams.height = 1000

        binding.barChart.animateY(1000)
        binding.barChart.invalidate()
    }

    private fun setupObservers() {
        viewModel.statsData.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                observeChartData()
                binding.progressBar.visibility = View.GONE
                binding.statsContainer.visibility = View.VISIBLE
            }
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

        binding.barChart.apply {
            data = BarData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
            invalidate()
        }
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        binding.statsContainer.visibility = View.GONE
        viewModel.loadAllStats()
    }

    private fun updatePieChart(statusData: Map<String, Int>) {
        val entries = statusData.map { PieEntry(it.value.toFloat(), it.key) }
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.status_pending),
                ContextCompat.getColor(requireContext(), R.color.status_approved),
                ContextCompat.getColor(requireContext(), R.color.status_processing),
            )
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            setDrawValues(true)
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            invalidate()
        }
    }

    private fun updateBarChart(weeklyData: Map<String, Int>) {
        val entries = weeklyData.values.mapIndexed { index, value ->
            BarEntry(index.toFloat(), value.toFloat())
        }

        val dataSet = BarDataSet(entries, "Issues").apply {
            color = ContextCompat.getColor(requireContext(), R.color.status_pending)
            valueTextSize = 10f
            setDrawValues(true)
        }

        binding.barChart.apply {
            data = BarData(dataSet)
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return weeklyData.keys.elementAtOrNull(value.toInt()) ?: ""
                }
            }
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}