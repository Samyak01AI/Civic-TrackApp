package com.example.civic_trackapplication.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch
import kotlin.collections.get
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ReportViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val issuesCollection = firestore.collection("Issues")

    private val _submissionStatus = MutableLiveData<SubmissionStatus>()
    val submissionStatus: LiveData<SubmissionStatus> = _submissionStatus

    fun submitIssue(
        title: String,
        description: String,
        location: String,
        category: String,
        imageUri: Uri?
    ) {
        _submissionStatus.value = SubmissionStatus.Loading

        viewModelScope.launch {
            try {
                // 1. Upload image if exists
                val imageUrl = imageUri?.let { uploadImage(it) }

                // 2. Create issue data
                val issue = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "location" to location,
                    "category" to category,
                    "status" to "pending",
                    "timestamp" to Timestamp.Companion.now(),
                    "imageUrl" to imageUrl,
                    "userId" to FirebaseAuth.getInstance().currentUser?.uid
                )

                // 3. Save to Firestore
                issuesCollection.add(issue)
                    .addOnSuccessListener {
                        _submissionStatus.value = SubmissionStatus.Success
                    }
                    .addOnFailureListener { e ->
                        _submissionStatus.value = SubmissionStatus.Error(e.message ?: "Unknown error")
                    }
            } catch (e: Exception) {
                _submissionStatus.value = SubmissionStatus.Error(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun uploadImage(imageUri: Uri): String = suspendCoroutine { cont ->
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
                    cont.resume(imageUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e("Cloudinary", "Upload error: ${error.description}")

                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.e("Cloudinary", "Rescheduled: ${error.description}")
                }
            })
            .dispatch()
    }

    sealed class SubmissionStatus {
        object Loading : SubmissionStatus()
        object Success : SubmissionStatus()
        class Error(val message: String) : SubmissionStatus()
    }
}