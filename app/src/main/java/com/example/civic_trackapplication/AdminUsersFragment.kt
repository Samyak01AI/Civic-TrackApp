package com.example.civic_trackapplication

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.civic_trackapplication.adapter.AdminUsersAdapter
import com.example.civic_trackapplication.databinding.FragmentAdminUsersBinding
import com.example.civic_trackapplication.viewmodels.AdminUsersViewModel
import com.example.civic_trackapplication.viewmodels.ProfileViewModel
import com.example.civic_trackapplication.viewmodels.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.getValue

class AdminUsersFragment : Fragment() {
    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminUsersViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var adapter: AdminUsersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = "Manage Users"
        adapter = AdminUsersAdapter { userId, isAdmin ->
            viewModel.updateAdminStatus(userId, isAdmin)
        }

        setupRecyclerView()
        setupObservers()
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdminUsersFragment.adapter
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    private fun setupObservers() {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
        }

        viewModel.updateStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is Resource.Success -> {
                    adapter.clearPendingUpdates()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), status.message, Toast.LENGTH_SHORT).show()
                    viewModel.loadUsers() // Refresh on error
                }
                is Resource.Loading -> { /* Handle loading state */ }
            }
        }
    }

    private fun showLogoutConfirmation() {
        val pref = requireContext().getSharedPreferences("user_pref", MODE_PRIVATE)
        pref.edit().apply {
            putBoolean("isLoggedIn", false)
            apply()
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                profileViewModel.logout()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish() // Optional: finish current activity if logging out

            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}