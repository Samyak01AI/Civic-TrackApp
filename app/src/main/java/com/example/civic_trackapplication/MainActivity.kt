package com.example.civic_trackapplication

import NetworkMonitor
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.civic_trackapplication.viewmodels.AuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.messaging.FirebaseMessaging
import java.util.logging.Handler
import kotlin.getValue

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var networkMonitor: NetworkMonitor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val chatbotButton = findViewById<ImageButton>(R.id.chatbot)
        val webView = findViewById<WebView>(R.id.chat_webview)

        FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "✅ Token: $token")
                // Optionally send token to Firestore or your server
            } else {
                Log.e("FCM", "❌ Failed to get token", task.exception)
            }
        }

        FirebaseMessaging.getInstance().subscribeToTopic("issues")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "✅ Subscribed to topic: issues")
                }
            }

        chatbotButton.setOnClickListener {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END)
            drawerLayout.openDrawer(GravityCompat.END)
        }
        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                webView.settings.javaScriptEnabled = true
                webView.settings.domStorageEnabled = true
                webView.loadUrl("https://cdn.botpress.cloud/webchat/v2.4/shareable.html?configUrl=https://files.bpcontent.cloud/2025/05/10/04/20250510041211-1NHXRUS9.json")
            }
        })

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)
    }
    private var connectionDialog: Dialog? = null

    fun showNoConnectionDialog(context: Context) {
        if (connectionDialog?.isShowing == true) return // Already shown

        connectionDialog = Dialog(context).apply {
            setContentView(R.layout.popup_connection_status)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            setCancelable(false) // Prevent manual dismissal

            val lottieView = findViewById<LottieAnimationView>(R.id.lottieStatus)
            lottieView.setAnimation("disconnected.json")
            lottieView.playAnimation()

            show()
        }
    }

    fun dismissConnectionDialog() {
        connectionDialog?.dismiss()
        connectionDialog = null
    }
    fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onStart() {
        super.onStart()
        networkMonitor = NetworkMonitor(this)

        networkMonitor.startMonitoring()

        // Observe connectivity changes
        networkMonitor.isConnected.observe(this) { isConnected ->
            if (isConnected) {
                dismissConnectionDialog()
            } else {
                showNoConnectionDialog(this)
            }
        }

        if (!isInternetAvailable(this)) {
            showNoConnectionDialog(this)
        }
    }

}