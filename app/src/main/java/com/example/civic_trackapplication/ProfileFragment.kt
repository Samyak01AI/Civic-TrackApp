package com.example.civic_trackapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.civic_trackapplication.adapter.ProfileIssuesAdapter
import com.example.civic_trackapplication.databinding.FragmentProfileBinding
import com.example.civic_trackapplication.viewmodels.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var issuesAdapter: ProfileIssuesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeData()
    }

    private fun setupRecyclerView() {
        issuesAdapter = ProfileIssuesAdapter { issue ->
            navigateToIssueDetail(issue.id)
        }

        binding.rvIssues.apply {
            adapter = issuesAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {

        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkModeEnabled(isChecked)
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }
    private fun navigateToIssueDetail(issueId: String) {
        val intent = Intent(requireContext(), IssueDetailActivity::class.java).apply {
            putExtra("ISSUE_ID", issueId)
        }
        startActivity(intent)
    }


    private fun showEditDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etName = view.findViewById<EditText>(R.id.etEditName)
        val etEmail = view.findViewById<EditText>(R.id.etEditEmail)

        etName.setText(binding.tvName.text.toString())
        etEmail.setText(binding.tvEmail.text.toString())

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(false)
            .create()

        view.findViewById<Button>(R.id.btnConfirmEdit).setOnClickListener {
            confirmSave(etName.text.toString(), etEmail.text.toString())
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.btnCancelEdit).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun confirmSave(newName: String, newEmail: String) {
        val view = layoutInflater.inflate(R.layout.dialog_confirm_changes, null)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(false)
            .create()

        view.findViewById<Button>(R.id.btnYes).setOnClickListener {
            saveChanges(newName, newEmail)
            alertDialog.dismiss()
        }

        view.findViewById<Button>(R.id.btnNo).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun saveChanges(newName: String, newEmail: String) {
        val pref = requireContext().getSharedPreferences("user_pref", MODE_PRIVATE)
        pref.edit().apply {
            putString("name", newName)
            putString("email", newEmail)
            apply()
        }
        loadProfile()
        Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun showImageOptionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Set Profile Picture")
        builder.setItems(arrayOf("Select Default Image", "Choose from Gallery")) { dialog, which ->
            when (which) {
                0 -> showDefaultImagesDialog()
                1 -> openImagePicker()
            }
            dialog.dismiss()
        }
        builder.create().show()
    }
    private fun loadProfile() {
        val pref = requireContext().getSharedPreferences("user_pref", MODE_PRIVATE)
        binding.tvName.text = pref.getString("name", "No Name") ?: "No Name"
        binding.tvEmail.text = pref.getString("email", "No Email") ?: "No Email"
        val imageUriString = pref.getString("profile_image", null)


        if (imageUriString != null) {
            val imageFile = File(Uri.parse(imageUriString).path ?: "")
            if (imageFile.exists()) {
                binding.ivProfile.setImageURI(Uri.fromFile(imageFile))
            } else {
                binding.ivProfile.setImageResource(R.drawable.ic_profile) // fallback
            }
        } else {
            binding.ivProfile.setImageResource(R.drawable.ic_profile)
        }
    }
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
               val selectedImageUri = uri
                binding.ivProfile.apply {
                    visibility = View.VISIBLE
                    setImageURI(uri)
                }
            }
        }
    }
    private fun showDefaultImagesDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_default_images, null)
        val img1 = view.findViewById<ImageView>(R.id.imgOption1)
        val img2 = view.findViewById<ImageView>(R.id.imgOption2)
        val img3 = view.findViewById<ImageView>(R.id.imgOption3)
        val img4 = view.findViewById<ImageView>(R.id.imgOption4)
        val img5 = view.findViewById<ImageView>(R.id.imgOption5)
        val img6 = view.findViewById<ImageView>(R.id.imgOption6)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        val clickListener = { resId: Int ->
            binding.ivProfile.setImageResource(resId)
            val pref = requireContext().getSharedPreferences("user_pref", MODE_PRIVATE)
            pref.edit().remove("profile_image").apply() // Remove URI if any
            dialog.dismiss()
        }

        img1.setOnClickListener { clickListener(R.drawable.dp1) }
        img2.setOnClickListener { clickListener(R.drawable.dp2) }
        img3.setOnClickListener { clickListener(R.drawable.dp3) }
        img4.setOnClickListener { clickListener(R.drawable.dp4) }
        img5.setOnClickListener { clickListener(R.drawable.dp5) }
        img6.setOnClickListener { clickListener(R.drawable.dp6) }

        dialog.show()
    }
    private fun observeData() {
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvName.text = it.name
                binding.tvEmail.text = it.email
                binding.tvJoinDate.text = "Member since ${it.joinDate}"
                Glide.with(this)
                    .load(it.photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_user_placeholder)
                    .into(binding.ivProfile)
            }
        }

        viewModel.userIssues.observe(viewLifecycleOwner) { issues ->
            issuesAdapter.submitList(issues)
            binding.tvEmptyState.visibility = if (issues.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isDarkMode.observe(viewLifecycleOwner) { isDark ->
            binding.switchDarkMode.isChecked = isDark
        }
    }


    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                viewModel.logout()
               // findNavController().navigate(R.id.action_profileFragment_to_loginActivity)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }
}