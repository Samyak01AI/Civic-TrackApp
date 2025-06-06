package com.example.civic_trackapplication

import NetworkMonitor
import android.app.Activity
import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.civic_trackapplication.adapter.ProfileIssuesAdapter
import com.example.civic_trackapplication.adapter.StatusIssuesAdapter
import com.example.civic_trackapplication.databinding.FragmentProfileBinding
import com.example.civic_trackapplication.viewmodels.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    var imageUrl = ""
    private lateinit var issuesAdapter: ProfileIssuesAdapter
    private lateinit var networkMonitor: NetworkMonitor

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

        val isDarkModeEnabled = when (
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        ) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            else -> false
        }

        binding.switchDarkMode.isChecked = isDarkModeEnabled
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkModeEnabled(isChecked)
        }

        observeData()

        loadProfileImage()
    }

    private fun loadProfileImage() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val pref = requireContext().getSharedPreferences("user_pref", MODE_PRIVATE)

        // First try loading cached image URL from SharedPreferences
        val cachedUrl = pref.getString("profile_image", null)
        if (!cachedUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(cachedUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_user_placeholder)
                .into(binding.ivProfile)
        }

        // Then also fetch latest from Firestore to keep in sync
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val url = doc.getString("photoUrl")
                if (!url.isNullOrEmpty() && url != cachedUrl) {
                    pref.edit().putString("profile_image", url).apply() // update cache

                    Glide.with(this)
                        .load(url)
                        .circleCrop()
                        .placeholder(R.drawable.ic_user_placeholder)
                        .into(binding.ivProfile)
                }
            }
    }


    private fun setupRecyclerView() {
        issuesAdapter = ProfileIssuesAdapter(
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

    private fun setupClickListeners() {
        binding.ivProfile.setOnClickListener {
            showImageOptionDialog()
        }
        binding.btnEditProfile.setOnClickListener {
            showEditDialog()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
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
        viewModel.userData.observe(viewLifecycleOwner) { userProfile ->
            val pref = requireContext().getSharedPreferences("user_pref", MODE_PRIVATE)
            binding.tvName.text = pref.getString("name", "No Name") ?: "No Name"
            binding.tvEmail.text = pref.getString("email", "No Email") ?: "No Email"
            binding.tvJoinDate.text = "Member since ${formatTimestampToMonthDate(userProfile.joinDate)}"
            val cloudinaryUrl = userProfile.imageUrl

            if (!cloudinaryUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(cloudinaryUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_user_placeholder)
                    .into(binding.ivProfile)
            }
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
                Glide.with(this)
                    .load(selectedImageUri)
                    .circleCrop()
                    .placeholder(R.drawable.ic_user_placeholder)
                    .into(binding.ivProfile)
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
            var currentImageIdentifier: String? = null

            val newIdentifier = "drawable_$resId"

            if (newIdentifier == currentImageIdentifier) {
                dialog.dismiss()
            }

            Glide.with(this)
                .load(resId)
                .circleCrop()
                .placeholder(R.drawable.ic_user_placeholder)
                .into(binding.ivProfile)

            val appContext = requireContext().applicationContext
            CoroutineScope(Dispatchers.IO).launch {
                val uploadedUrl = uploadImage(getUriFromDrawable(resId))
                    imageUrl = uploadedUrl
                    val pref = appContext.getSharedPreferences("user_pref", MODE_PRIVATE)
                    pref.edit().putString("profile_image", imageUrl).apply()

            }
            FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .update("photoUrl", imageUrl)
            loadProfile()
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

    suspend fun uploadImage(imageUri: Uri): String = suspendCoroutine { cont ->
        MediaManager.get().upload(imageUri)
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d("Cloudinary", "Upload started")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    Log.d("Cloudinary", "Uploading: $bytes / $totalBytes")
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"] as String
                    cont.resume(imageUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e("Cloudinary", "Upload error: ${error.description}")

                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.e("Cloudinary", "Rescheduled: ${error.description}")
                }
            })
            .dispatch()
    }
    fun getUriFromDrawable(resId: Int): Uri {
        val drawable = ContextCompat.getDrawable(requireContext(), resId) ?: return Uri.EMPTY
        val bitmap = (drawable as BitmapDrawable).bitmap
        val file = File(requireContext().cacheDir, "profile_temp.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
        return FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
    }



    private fun observeData() {
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvName.text = it.name
                binding.tvEmail.text = it.email
                binding.tvJoinDate.text = "Member since ${formatTimestampToMonthDate(it.joinDate)}"
                val cloudinaryUrl = it.imageUrl
                if (!cloudinaryUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(cloudinaryUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_user_placeholder)
                        .into(binding.ivProfile)
                }
                viewModel.fetchUserIssues()
            }
        }

        viewModel.userIssues.observe(viewLifecycleOwner) { issues ->
            issuesAdapter.submitList(issues)
            viewModel.fetchUserIssues()
        }

        viewModel.isDarkMode.observe(viewLifecycleOwner) { isDark ->
            binding.switchDarkMode.isChecked = isDark
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
                viewModel.logout()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish() // Optional: finish current activity if logging out

            }
            .setNegativeButton("Cancel", null)
            .show()
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

    fun formatTimestampToMonthDate(timestamp: Date?): String {
        val date = timestamp ?: return ""
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(date)
    }


    override fun onStart() {
        super.onStart()
        networkMonitor = NetworkMonitor(requireContext())
        networkMonitor.isConnected.observe(this) { isConnected ->
            if (!isConnected) {
                Toast.makeText(requireContext(), "Please check internet connection", Toast.LENGTH_SHORT).show()
            }
        }
        networkMonitor.startMonitoring()
    }

    override fun onStop() {
        super.onStop()
        networkMonitor.stopMonitoring()
    }
    override fun onResume() {
        super.onResume()
        val isDarkModeEnabled = when (
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        ) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            else -> false
        }
        Log.d("ProfileFragment", "isDarkModeEnabled: $isDarkModeEnabled")
        binding.switchDarkMode.isChecked = isDarkModeEnabled
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }
}