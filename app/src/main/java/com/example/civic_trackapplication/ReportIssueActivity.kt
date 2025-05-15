package com.example.civic_trackapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
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
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.core.net.toUri
import com.example.civic_trackapplication.viewmodels.LocationViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportIssueActivity : AppCompatActivity() {

    private lateinit var viewModel: LocationViewModel
    companion object {
        const val REQUEST_CAMERA = 100
    }

    var etDescription: EditText? = null
    var spinnerCategory: Spinner? = null
    var btnSubmit: Button? = null
    var locationButton: ImageButton? = null
    var imagePreview: ImageView? = null
    var btnUploadImage: Button? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationText: TextView
    lateinit var imageUri: Uri
    lateinit var location : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_issue)

        etDescription = findViewById<EditText>(R.id.inputDescription)
        spinnerCategory = findViewById<Spinner>(R.id.categorySpinner)
        btnSubmit = findViewById<Button>(R.id.btnSubmit)
        locationButton = findViewById<ImageButton>(R.id.locationButton)
        imagePreview = findViewById<ImageView>(R.id.imagePreview)
        btnUploadImage = findViewById<Button>(R.id.btnUploadImage)
        locationText = findViewById<TextView>(R.id.locationText)


        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.issue_categories,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory?.adapter = adapter

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // TODO: Add location fetching
        viewModel = ViewModelProvider(this)[LocationViewModel::class.java]

        viewModel.selectedLocation.observe(this) { latLng ->
            if (latLng != null) {
                val lat = latLng.latitude
                val lng = latLng.longitude
                Log.d("ActivityLocation", "Lat: $lat, Lng: $lng")
                locationText.text = "Latitude: $lat, Longitude: $lng"
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
                    .addOnSuccessListener { loc: Location? ->
                        if (loc != null) {
                            location = "Lat: ${loc.latitude}, Lng: ${loc.longitude}"
                            locationText.text = location
                        } else {
                            locationText.text = "Location not available"
                        }
                    }
                showMapDialog()
            }
        }

        btnSubmit?.setOnClickListener {
            val desc = etDescription?.text.toString().trim()
            val category = spinnerCategory?.selectedItem.toString()
            val location = locationText.text.toString()
            val imageUri = imageUri

            if (desc.isEmpty() || category == "Select Category" || location.isEmpty() || imageUri == null) {
                Log.d("ReportIssueActivity", "Description: $desc, Category: $category, Location: $location, ImageUri: $imageUri")
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                saveIssueToFirestore(desc, category, location)
            }
        }
    }

    fun showMapDialog() {
            val dialog = MapFragment()
            dialog.show(supportFragmentManager, "MapDialog")
        }
    private fun launchCamera() {
        val photoFile = createImageFile()
        imageUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, REQUEST_CAMERA)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
            imagePreview?.setImageBitmap(bitmap)
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


    fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    suspend fun uploadImageToCloudinary(imageUri: Uri?) : String = suspendCoroutine { cont ->
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
                    Log.d("Cloudinary", "Upload success: $imageUrl")
                    Toast.makeText(this@ReportIssueActivity, "Upload successful", Toast.LENGTH_SHORT).show()
                    cont.resume(imageUrl)
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

    private fun saveIssueToFirestore(title: String, category: String, locationStr: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val latLng = locationStr.replace("Lat:", "").replace("Lng:", "").split(",")
        val latitude = latLng.getOrNull(0)?.trim() ?: ""
        val longitude = latLng.getOrNull(1)?.trim() ?: ""

        if (imageUri != null) {
            lifecycleScope.launch {
                try {
                    val url = uploadImageToCloudinary(imageUri)
                    Log.d("Cloudinary", "Image URL: $url")
                    uploadIssue(db, title, category, latitude, longitude, userId, url)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()

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

        Log.d("Firestore", "Uploading issue: $issue")

        db.collection("Issues")
            .add(issue)
            .addOnSuccessListener {
                sendIssueNotification(title)
                Toast.makeText(this, "Issue submitted successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to submit issue: ${it.message}", Toast.LENGTH_SHORT).show()
                Log.e("FirestoreError", "Submit failed", it)
            }
    }

    private fun sendIssueNotification(message: String) {
        val url = "https://REGION-PROJECT_ID.cloudfunctions.net/send_issue_notification"

        val json = JSONObject()
        json.put("userId", FirebaseAuth.getInstance().uid)
        json.put("message", message)

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HTTP", "Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("HTTP", "Response: ${response.body?.string()}")
            }
        })
    }


}