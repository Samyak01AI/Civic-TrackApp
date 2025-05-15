package com.example.civic_trackapplication

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

class CloudinaryInit : Application() {
    override fun onCreate() {
        super.onCreate()

        val config: HashMap<String, String> = hashMapOf(
            "cloud_name" to "dzjgag3if",
            "api_key" to "989317572874918",
            "api_secret" to "abrpVoOpVLQBx7pxoNj2XDiqcdQ",
            "upload_preset" to "issuesimg"
        )
        MediaManager.init(this, config)
    }

}