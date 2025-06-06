package com.example.civic_trackapplication

import android.Manifest
import android.content.pm.PackageManager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.civic_trackapplication.viewmodels.LocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : DialogFragment(), OnMapReadyCallback {
    private lateinit var viewModel: LocationViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var googleMap: GoogleMap?= null
    private var currentMarker: Marker? = null
    private var selectedLocation: LatLng? = null
    private var marker: Marker? = null




    private val callback = OnMapReadyCallback { googleMap ->


        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
        googleMap.uiSettings.isRotateGesturesEnabled = true
        googleMap.uiSettings.isScrollGesturesEnabled = true



        googleMap.setOnInfoWindowClickListener(object : GoogleMap.OnInfoWindowClickListener {
            override fun onInfoWindowClick(p0: Marker) {

                Toast.makeText(requireContext(), "" + p0.tag.toString(), Toast.LENGTH_SHORT).show()
            }
        })
        googleMap.setOnMapClickListener { latLng ->
            selectedLocation = latLng

            // Remove old marker if exists
            marker?.remove()

            // Add new marker
            marker =
                googleMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))

            // Animate camera to marker
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

            // Save to shared ViewModel
            viewModel.setSelectedLocation(latLng)

            Log.d("MapFragment", "Marked Location: $latLng")
        }
    }


        override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            dpToPx(400).toInt(), // Width in dp
            dpToPx(600).toInt()  // Height in dp (same as width → square)
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mapFragment?.getMapAsync(callback)
        mapFragment?.getMapAsync {
            map ->
            googleMap = map
            setMapUi()
        }

    }

    private fun setMapUi() {

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            googleMap?.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
        }

        val btnMapType = view?.findViewById<ImageButton>(R.id.btnMapType)
        val popupView = layoutInflater.inflate(R.layout.map_type_popup, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.elevation = 10f

        btnMapType?.setOnClickListener {
            popupWindow.showAsDropDown(btnMapType, -100, 0)  // Adjust offset as needed
        }

        popupView.findViewById<ImageView>(R.id.iconNormal).setOnClickListener {
            googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            popupWindow.dismiss()
        }
        popupView.findViewById<ImageView>(R.id.iconSatellite).setOnClickListener {
            googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            popupWindow.dismiss()
        }
        popupView.findViewById<ImageView>(R.id.iconTerrain).setOnClickListener {
            googleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            popupWindow.dismiss()
        }
        popupView.findViewById<ImageView>(R.id.iconHybrid).setOnClickListener {
            googleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            popupWindow.dismiss()
        }

    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
    }

}