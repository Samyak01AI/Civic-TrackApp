package com.example.civic_trackapplication

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class ReportIssueActivity : AppCompatActivity() {

    lateinit var googleMap: GoogleMap
    private var selectedMarker: Marker? = null
    private var selectedLatLng: LatLng? = null
    private lateinit var viewModel: LocationViewModel

    companion object {
        private const val REQUEST_CAMERA = 100
        const val REQUEST_LOCATION = 200
    }

    var etDescription: EditText? = null
    var spinnerCategory: Spinner? = null
    var btnSubmit: Button? = null
    var tvLocation: TextView? = null
    var locationButton: ImageButton? = null
    var imagePreview: ImageView? = null
    var btnUploadImage: Button? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationText: TextView
    lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_issue)

        etDescription = findViewById<EditText>(R.id.inputDescription)
        tvLocation = findViewById<TextView>(R.id.locationText)
        spinnerCategory = findViewById<Spinner>(R.id.categorySpinner)
        btnSubmit = findViewById<Button>(R.id.btnSubmit)
        locationButton = findViewById<ImageButton>(R.id.locationButton)
        imagePreview = findViewById<ImageView>(R.id.imagePreview)
        btnUploadImage = findViewById<Button>(R.id.btnUploadImage)
        locationText = findViewById<TextView>(R.id.locationText)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // TODO: Add location fetching
        viewModel = ViewModelProvider(this)[LocationViewModel::class.java]

        viewModel.selectedLocation.observe(this) { latLng ->
            if (latLng != null) {
                val lat = latLng.latitude
                val lng = latLng.longitude
                Log.d("ActivityLocation", "Lat: $lat, Lng: $lng")
                tvLocation?.text = "Latitude: $lat, Longitude: $lng"
                // Use it here: update UI, save to Firebase, etc.
            }
        }

        btnUploadImage?.setOnClickListener {
            // TODO: Add CameraX integration
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA
                )
            } else {
                launchCamera()
            }
        }



        locationButton?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1001
                )
            } else {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            val loc = "Lat: ${location.latitude}, Lng: ${location.longitude}"
                            locationText.text = loc
                        } else {
                            locationText.text = "Location not available"
                        }
                    }

                showMapDialog()
            }
        }

        btnSubmit?.setOnClickListener {
            uploadImageToCloudinary(imageUri)
        }

        // TODO: Connect to Firestore
    }

    fun showMapDialog() {
            val dialog = MapFragment()
            dialog.show(supportFragmentManager, "MapDialog")
        }
    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            bitmap.let {
                imagePreview?.setImageBitmap(it)
                imagePreview?.tag = it
                imageUri = Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, it, "title", null))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    fun uploadImageToCloudinary(imageUri: Uri?) {
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
                    val url = resultData["secure_url"] as? String
                    Log.d("Cloudinary", "Upload success: $url")
                    Toast.makeText(this@ReportIssueActivity, "Upload successful", Toast.LENGTH_SHORT).show()

                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e("Cloudinary", "Upload error: ${error.description}")
                    Toast.makeText(this@ReportIssueActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.e("Cloudinary", "Rescheduled: ${error.description}")
                }
            })
            .dispatch()
    }

    fun getCloudinaryImageUrlManually(cloudName: String, publicId: String): String {
        return "https://res.cloudinary.com/$cloudName/image/upload/$publicId"
    }

}