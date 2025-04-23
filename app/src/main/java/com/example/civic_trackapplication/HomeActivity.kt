package com.example.civic_trackapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.civic_trackapplication.R

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val name = intent.getStringExtra("username") ?: "User"
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        welcomeText.text = "Hi, $name 👋"
    }
}
