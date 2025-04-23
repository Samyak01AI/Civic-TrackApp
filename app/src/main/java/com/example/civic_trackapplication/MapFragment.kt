package com.example.civic_trackapplication

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.internal.ViewUtils.dpToPx

class MapFragment : DialogFragment() {
    private lateinit var googleMap: GoogleMap
    private lateinit var viewModel: LocationViewModel
    private var currentMarker: Marker? = null
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        val location = LatLng(28.6139, 77.2090)  // Delhi


        // Move and zoom the camera to the marker
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        googleMap.mapType=GoogleMap.MAP_TYPE_NORMAL
        googleMap.uiSettings.isZoomControlsEnabled=true
        googleMap.uiSettings.isCompassEnabled=true
        googleMap.uiSettings.isMapToolbarEnabled=true
        googleMap.uiSettings.isRotateGesturesEnabled=true

        googleMap.setOnInfoWindowClickListener(object :GoogleMap.OnInfoWindowClickListener{
            override fun onInfoWindowClick(p0: Marker) {

                Toast.makeText(requireContext(),""+p0.tag.toString(),Toast.LENGTH_SHORT).show()
            }
        })
        googleMap.setOnMapClickListener { latLng ->
            currentMarker?.remove()
            currentMarker = googleMap.addMarker(
                MarkerOptions().position(latLng).title("Selected Location")
            )
            viewModel.setLocation(latLng)
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
        mapFragment?.getMapAsync(callback)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}