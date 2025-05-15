package com.example.civic_trackapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.civic_trackapplication.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val viewModel: AuthViewModel by viewModels()
        viewModel.checkAdminStatus()
        lifecycleScope.launch {
            delay(1500)
            val pref = getSharedPreferences("user_pref", MODE_PRIVATE)
            val isLoggedIn = pref.getBoolean("isLoggedIn", false)
            viewModel.isAdmin.observe(this@SplashActivity) { isAdmin ->
                if (isAdmin) {
                    val intent = Intent(this@SplashActivity, AdminActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else if (isLoggedIn) {
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
}