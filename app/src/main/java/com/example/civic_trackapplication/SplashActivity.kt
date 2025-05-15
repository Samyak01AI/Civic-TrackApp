package com.example.civic_trackapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            delay(1500)
            val pref = getSharedPreferences("user_pref", MODE_PRIVATE)
            val isLoggedIn = pref.getBoolean("isLoggedIn", false)

            if (isLoggedIn) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))
                    finish()
                }, 1500)
            }
        }


    }
}