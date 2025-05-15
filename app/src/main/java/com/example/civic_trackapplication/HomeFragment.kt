package com.example.civic_trackapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.civic_trackapplication.adapter.IssuesAdapter
import com.example.civic_trackapplication.databinding.FragmentHomeBinding
import com.example.civic_trackapplication.viewmodels.IssuesViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: IssuesViewModel by viewModels()
    private lateinit var issuesAdapter: IssuesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        observeIssues()
    }

    private fun setupRecyclerView() {
        issuesAdapter = IssuesAdapter { issue ->
            // Handle item click
            navigateToIssueDetail(issue.id)
        }
        binding.rvIssues.apply {
            adapter = issuesAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.HORIZONTAL))
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshIssues()
        }
    }

    private fun observeIssues() {
        viewModel.issues.observe(viewLifecycleOwner) { issues ->
            issuesAdapter.submitList(issues)
            binding.swipeRefresh.isRefreshing = false

            if (issues.isEmpty()) {
                showEmptyState()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
         //   binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun showEmptyState() {
        binding.rvIssues.visibility = View.GONE
    }


    private fun navigateToIssueDetail(issueId: String) {
        // Safe navigation with Navigation Component
        val directions = HomeFragmentDirections.actionHomeFragmentToIssueDetailFragment(issueId)
        findNavController().navigate(directions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}