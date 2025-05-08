package com.example.civic_trackapplication

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.GeoPoint

data class Issue(
    var title: String = "",
    var status: String = "",
    var category: String = "",
    var imageUrl: String = "",
    var location: Map<String, String> = emptyMap()
)