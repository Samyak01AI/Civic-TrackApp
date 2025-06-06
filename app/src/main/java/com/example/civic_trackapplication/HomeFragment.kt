package com.example.civic_trackapplication

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.civic_trackapplication.adapter.IssuesAdapter
import com.example.civic_trackapplication.databinding.FragmentHomeBinding
import com.example.civic_trackapplication.viewmodels.IssuesViewModel
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.example.civic_trackapplication.adapter.AdminIssuesAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: IssuesViewModel by viewModels()
    val auth = Firebase.auth
    val firestore = Firebase.firestore
    var address : String = ""
    private lateinit var issuesAdapter: IssuesAdapter
    private val _issueStatusCount = MutableLiveData<IssueStatusCount>()

    private val PERMISSION_CODE = 123

    private val requiredPermissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

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

        requestPermissionsIfNecessary()
        setupRecyclerView()
        setupSwipeRefresh()
        observeIssues()
        val pref = requireContext().getSharedPreferences("user_pref", MODE_PRIVATE)
        binding.tvLocation.text = pref.getString("location", "")
        loadIssueStatusCount()

        binding.cardHelpSupport.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHelpSupportFragment())
        }
        binding.cardRateUs.setOnClickListener {
            showRateUsDialog(requireContext())
        }
        binding.cardAboutUs.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAboutUsFragment())
        }
        binding.cardContactUs.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToContactUsFragment())
        }
    }

    private fun setupRecyclerView() {
        issuesAdapter = IssuesAdapter(
            onItemClick = { issue ->
                showIssueDetailsDialog(issue)
            }
        )
        binding.rvIssues.apply {
            adapter = issuesAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.HORIZONTAL))
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            observeIssues()
        }
    }

    private fun observeIssues() {
        viewModel.issues.observe(viewLifecycleOwner) { issues ->
            issuesAdapter.submitList(issues)
            issuesAdapter.updateList(issues)
            binding.swipeRefresh.isRefreshing = false

            if (issues.isEmpty()) {
                showEmptyState()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }
    }
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                Log.d("Location", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                address = getAddressFromLatLng(location.latitude, location.longitude)
                val pref = requireContext().getSharedPreferences("user_pref", MODE_PRIVATE)
                pref.edit { putString("location", address) }
            } else {
                Log.d("Location", "Location is null")
            }
        }
    }
    fun getAddressFromLatLng(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                // You can customize how the address string is formed
                address.getAddressLine(0) ?: "Unknown Address"
            } else {
                "Address not found"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Error retrieving address"
        }
    }
    fun loadIssueStatusCount() {
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("Issues")
                .get()
                .addOnSuccessListener { snapshot ->
                    var resolved = 0
                    var pending = 0

                    for (doc in snapshot.documents) {
                        when (doc.getString("status") ?: "pending") {
                            "Resolved" -> resolved++
                            else -> pending++
                        }
                    }
                    binding.tvResolvedCount.text = resolved.toString()
                    binding.tvPendingCount.text = pending.toString()
                    Log.d("ProfileViewModel", "Resolved: $_issueStatusCount, Pending: $pending")
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileViewModel", "Error counting issues", e)
                    _issueStatusCount.value = IssueStatusCount()
                }
        }
        }
    private fun getPermissionsList(): Array<String> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return requiredPermissions.toTypedArray()
    }

    private fun requestPermissionsIfNecessary() {
        val missing = getPermissionsList().filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), missing.toTypedArray(), PERMISSION_CODE)
        } else {
            // All permissions granted
            onPermissionsGranted()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_CODE) {
            val denied = permissions.zip(grantResults.toTypedArray())
                .filter { it.second != PackageManager.PERMISSION_GRANTED }

            if (denied.isEmpty()) {
                onPermissionsGranted()
            } else {
                Toast.makeText(requireContext(), "Please grant all permissions to proceed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onPermissionsGranted() {
        getCurrentLocation()
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

    private fun showEmptyState() {
        binding.rvIssues.visibility = View.GONE
    }

    fun showRateUsDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rate_us, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)

        val dialog = MaterialAlertDialogBuilder(
            context,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog
        )
            .setView(dialogView)
            .setPositiveButton("Submit") { dialogInterface, _ ->
                val rating = ratingBar.rating
                if (rating >= 4) {
                    Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Thanks for your feedback!", Toast.LENGTH_SHORT).show()
                }
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
            .create()

        dialog.show()

        // Customize button colors (optional)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(context, R.color.primary_blue))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
data class IssueStatusCount(
    val resolved: Int = 0,
    val pending: Int = 0
)