package com.example.civictrackapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.civic_trackapplication.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class ReportIssueActivity : AppCompatActivity() {

    private lateinit var imagePreview: ImageView
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var categorySpinner: Spinner
    private lateinit var locationText: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var capturedImageBitmap: Bitmap? = null

    companion object {
        private const val REQUEST_CAMERA = 100
        private const val REQUEST_LOCATION = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_issue)

        imagePreview = findViewById(R.id.imagePreview)
        descriptionInput = findViewById(R.id.inputDescription)
        categorySpinner = findViewById(R.id.categorySpinner)
        locationText = findViewById(R.id.locationText)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val categories = listOf("Select Category", "Garbage", "Pothole", "Water Issue", "Electricity")
        categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        findViewById<Button>(R.id.btnUploadImage).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
            } else {
                launchCamera()
            }
        }

        requestLocation()

        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            val desc = descriptionInput.text.toString().trim()
            val category = categorySpinner.selectedItem.toString()
            val location = locationText.text.toString()

            if (desc.isEmpty() || category == "Select Category") {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                saveIssueToFirestore(desc, category, location)
            }
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val loc = "Lat: ${it.latitude}, Lng: ${it.longitude}"
                        locationText.text = loc
                    } ?: run {
                        locationText.text = "Location not available"
                    }
                }
        }
    }

    private fun saveIssueToFirestore(title: String, category: String, locationStr: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val latLng = locationStr.replace("Lat:", "").replace("Lng:", "").split(",")
        val latitude = latLng.getOrNull(0)?.trim() ?: ""
        val longitude = latLng.getOrNull(1)?.trim() ?: ""

        if (capturedImageBitmap != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("issue_images/${UUID.randomUUID()}.jpg")

            val baos = ByteArrayOutputStream()
            capturedImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageData = baos.toByteArray()

            imageRef.putBytes(imageData)
                .continueWithTask { task ->
                    if (!task.isSuccessful) task.exception?.let { throw it }
                    imageRef.downloadUrl
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result.toString()
                        uploadIssue(db, title, category, latitude, longitude, userId, downloadUrl)
                    } else {
                        Toast.makeText(this, "Image upload failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        Log.e("UploadError", "Image upload failed", task.exception)
                    }
                }

        } else {
            uploadIssue(db, title, category, latitude, longitude, userId, "")
        }
    }

    private fun uploadIssue(
        db: FirebaseFirestore,
        title: String,
        category: String,
        latitude: String,
        longitude: String,
        userId: String,
        imageUrl: String
    ) {
        val issue = hashMapOf(
            "title" to title,
            "category" to category,
            "location" to hashMapOf(
                "latitude" to latitude,
                "longitude" to longitude
            ),
            "status" to "Pending",
            "submittedBy" to userId,
            "imageUrl" to imageUrl
        )

        db.collection("Issues")
            .add(issue)
            .addOnSuccessListener {
                Toast.makeText(this, "Issue submitted successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to submit issue: ${it.message}", Toast.LENGTH_SHORT).show()
                Log.e("FirestoreError", "Submit failed", it)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                capturedImageBitmap = it
                imagePreview.setImageBitmap(it)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation()
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
