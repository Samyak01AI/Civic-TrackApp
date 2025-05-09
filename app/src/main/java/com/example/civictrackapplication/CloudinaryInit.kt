package com.example.civictrackapplication

import android.app.Application
import com.cloudinary.android.MediaManager


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




