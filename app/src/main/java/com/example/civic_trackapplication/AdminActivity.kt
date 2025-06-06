package com.example.civic_trackapplication
import NetworkMonitor
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.civic_trackapplication.databinding.ActivityAdminBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var navController: NavController

    private lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
        FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.admin_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Connect BottomNavigationView with NavController
        binding.bottomNavigation.setupWithNavController(navController)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            Log.d("NAV_DEBUG", "Selected item: ${item.itemId}")
            when(item.itemId) {
                R.id.nav_issues -> {
                    navController.navigate(R.id.adminFragment)
                    true
                }
                R.id.nav_users -> {
                    navController.navigate(R.id.usersFragment)
                    true
                }
                R.id.nav_stats -> {
                    navController.navigate(R.id.statsFragment)
                    true
                }
                else -> false
            }
        }

        // Optional: Add fragment transition animations
        navHostFragment.navController.addOnDestinationChangedListener {
                _, destination, _ ->
            when (destination.id) {
                R.id.adminFragment -> binding.bottomNavigation.menu.findItem(R.id.nav_issues)?.isChecked = true
                R.id.usersFragment -> binding.bottomNavigation.menu.findItem(R.id.nav_users)?.isChecked = true
                R.id.statsFragment -> binding.bottomNavigation.menu.findItem(R.id.nav_stats)?.isChecked = true
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
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