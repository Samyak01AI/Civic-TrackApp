package com.example.civic_trackapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.civic_trackapplication.adapter.StatusAdapter
import com.example.civic_trackapplication.databinding.FragmentStatusBinding
import com.example.civic_trackapplication.viewmodels.StatusViewModel

class StatusFragment : Fragment() {
    private var _binding: FragmentStatusBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatusViewModel by viewModels()
    private lateinit var issuesAdapter: StatusAdapter

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
        setupSortOptions()
        observeViewModel()
        setupSwipeRefresh()
    }

    private fun setupRecyclerView() {
        issuesAdapter = StatusAdapter { issue ->
            navigateToIssueDetails(issue.id)
        }

        binding.rvIssues.apply {
            adapter = issuesAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val selectedStatus = when (checkedIds.firstOrNull()) {
                R.id.chip_all -> null
                R.id.chip_pending -> "pending"
                R.id.chip_in_progress -> "in_progress"
                R.id.chip_resolved -> "resolved"
                else -> null
            }
            viewModel.setStatusFilter(selectedStatus)
        }
    }

    private fun setupSortOptions() {
        binding.sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val sortField = when (pos) {
                    0 -> "timestamp" // Newest first
                    1 -> "title" // Alphabetical
                    else -> "timestamp"
                }
                viewModel.setSortOrder(sortField)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeViewModel() {
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
            viewModel.refreshData()
        }
    }

    private fun navigateToIssueDetails(issueId: String) {
        val directions = StatusFragmentDirections.actionStatusFragmentToIssueDetailFragment(issueId)
        findNavController().navigate(directions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}