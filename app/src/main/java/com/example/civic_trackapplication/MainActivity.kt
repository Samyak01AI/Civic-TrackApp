package com.example.civic_trackapplication
import com.google.firebase.FirebaseApp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        // Optional: Set welcome text
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        welcomeText.text = "Welcome to CivicTrack!"
    }
}
