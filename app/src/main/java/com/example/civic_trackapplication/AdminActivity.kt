package com.example.civic_trackapplication
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.civic_trackapplication.databinding.ActivityAdminBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
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
}