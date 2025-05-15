package com.example.civic_trackapplication.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class LocationViewModel : ViewModel() {
    private val _selectedLocation = MutableLiveData<LatLng?>()
    val selectedLocation: LiveData<LatLng?> get() = _selectedLocation

    fun setLocation(latLng: LatLng) {
        _selectedLocation.value = latLng
    }
}