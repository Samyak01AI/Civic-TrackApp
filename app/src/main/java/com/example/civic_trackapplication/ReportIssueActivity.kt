package com.example.civic_trackapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class ReportIssueActivity : AppCompatActivity() {

    lateinit var googleMap: GoogleMap
    private var selectedMarker: Marker? = null
    private var selectedLatLng: LatLng? = null
    private lateinit var viewModel: LocationViewModel

    companion object {
        private const val REQUEST_CAMERA = 100
        private const val REQUEST_LOCATION = 200
    }

    var etDescription : EditText? = null
    var spinnerCategory : Spinner? = null
    var btnSubmit : Button? = null
    var tvLocation : TextView? = null
    var locationButton : ImageButton? = null
    var imagePreview : ImageView? = null
    var btnUploadImage : Button? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationText: TextView


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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
            } else {
                launchCamera()
            }
        }



        locationButton?.setOnClickListener {
            showMapDialog()
        }
        btnSubmit?.setOnClickListener {
            finish()
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
        if (requestCode == com.example.civic_trackapplication.ReportIssueActivity.Companion.REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            imagePreview?.setImageBitmap(bitmap)
        }
    }
    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                com.example.civic_trackapplication.ReportIssueActivity.Companion.REQUEST_LOCATION
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
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            com.example.civic_trackapplication.ReportIssueActivity.Companion.REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation()
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            com.example.civic_trackapplication.ReportIssueActivity.Companion.REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}