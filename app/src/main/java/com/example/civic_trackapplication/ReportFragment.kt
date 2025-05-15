package com.example.civic_trackapplication

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.civic_trackapplication.databinding.FragmentReportBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.chip.Chip
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import com.example.civic_trackapplication.viewmodels.LocationViewModel
import com.example.civic_trackapplication.viewmodels.ReportViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val REQUEST_CAMERA = 100

class ReportFragment : Fragment() {
    // TODO: Rename and change types of parameters


    private var param1: String? = null
    private var param2: String? = null
    private val viewModel: ReportViewModel by viewModels()
    private lateinit var locationViewModel: LocationViewModel
    lateinit var imageUri: Uri

    private var selectedLocation: LatLng? = null
    lateinit var location : String
    lateinit var category : String
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageUri = "".toUri()
        category = ""
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]
        locationViewModel.selectedLocation.observe(viewLifecycleOwner) { latLng ->
            if (latLng != null) {
                val lat = latLng.latitude
                val lng = latLng.longitude
                Log.d("ActivityLocation", "Lat: $lat, Lng: $lng")
                binding.locationText.text = "Lat: $lat, Lng: $lng"
                selectedLocation = latLng
            }
        }
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.locationButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1001
            )
        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        location = "Lat: ${loc.latitude}, Lng: ${loc.longitude}"
                        binding.locationText.text = location
                    } else {
                        binding.locationText.text = "Location not available"
                    }
                }
            showMapDialog()
        }
        }
        binding.btnUploadImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA
            )
        } else {
            launchCamera()
        }
        }
        binding.btnSubmit.setOnClickListener {
            val desc = binding.etTitle.text.toString().trim()
            val location = binding.locationText.text.toString()

            binding.category.setOnClickListener {
                val selectedChipId = binding.category.checkedChipId
                if (selectedChipId != View.NO_ID) {
                    val selectedChip = binding.category.findViewById<Chip>(selectedChipId)
                    category = selectedChip.text.toString()
                }

            }

            if (desc.isEmpty() || category == "Select Category" || location.isEmpty() || imageUri.toString().equals("")) {
                Log.d("ReportFragment", "Description: $desc, Category: $category, Location: $location, ImageUri: $imageUri")
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.submitIssue(
                    title = binding.etTitle.text.toString(),
                    description = binding.etTitle.text.toString(),
                    location = selectedLocation!!,
                    imageUri = imageUri
                )
            }
        }

    }

    companion object {
        /**
        * Use this factory method to create a new instance of
        * this fragment using the provided parameters.
        *
        * @param param1 Parameter 1.
        * @param param2 Parameter 2.
        * @return A new instance of fragment ReportFragment.
        */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReportFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val REQUEST_CAMERA = 100
    }
    private fun launchCamera() {
        val photoFile = createImageFile()
        imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${context?.packageName}.fileprovider",
            photoFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    fun showMapDialog() {
        val dialog = MapFragment()
        dialog.show(childFragmentManager, "MapDialog")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            val bitmap = BitmapFactory.decodeStream(context?.contentResolver?.openInputStream(imageUri))
            binding.imagePreview.visibility = View.VISIBLE
            binding.imagePreview.setImageBitmap(bitmap)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            ReportIssueActivity.Companion.REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera()
                } else {
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun observeViewModel() {
        viewModel.submissionStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is ReportViewModel.SubmissionStatus.Success -> {
                    // binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Issue reported successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack() // Return to previous screen
                }
                is ReportViewModel.SubmissionStatus.Error -> {
                    // binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        "Error: ${status.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                ReportViewModel.SubmissionStatus.Loading -> {
                    //   binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks and crashes
    }
}
