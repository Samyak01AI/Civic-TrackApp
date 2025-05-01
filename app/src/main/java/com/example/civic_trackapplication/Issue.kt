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

data class Issue(
    val title: String,
    val location: String,
    val status: String,
    val category: String,
    val imageRes: Int
)