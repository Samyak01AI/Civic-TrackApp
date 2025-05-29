package com.example.civic_trackapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class MyAccountActivity : AppCompatActivity() {

    private lateinit var tvAccountName: TextView
    private lateinit var cardMyReports: LinearLayout
    private lateinit var cardProfile: LinearLayout
    private lateinit var cardNotifications: LinearLayout
    private lateinit var cardSubmitComplaint: LinearLayout
    private lateinit var cardHelpSupport: LinearLayout
    private lateinit var cardLogout: LinearLayout
    private lateinit var mGoogleSignInClient : GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_account)

        // Bind views
        tvAccountName = findViewById(R.id.tvAccountName)
        cardMyReports = findViewById(R.id.cardMyReports)
        cardProfile = findViewById(R.id.cardProfile)
        cardNotifications = findViewById(R.id.cardNotifications)
        cardSubmitComplaint = findViewById(R.id.cardSubmitComplaint)
        cardHelpSupport = findViewById(R.id.cardHelpSupport)
        cardLogout = findViewById(R.id.cardLogout)



        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        // Load user name dynamically
        val pref = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val name = pref.getString("name", "No Name")
        tvAccountName.text = name

        // Set click listeners
        cardMyReports.setOnClickListener {
            startActivity(Intent(this, MyReportsActivity::class.java))
        }

        cardProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        cardNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        cardSubmitComplaint.setOnClickListener {
            startActivity(Intent(this, ReportIssueActivity::class.java))
        }

/*        cardHelpSupport.setOnClickListener {
            startActivity(Intent(this, HelpSupportActivity::class.java))
        }*/

        cardLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { _, _ ->
            logout()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun logout() {
        val pref = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        pref.edit().apply {
            putBoolean("isLoggedIn", false)
            apply()
        }
        FirebaseAuth.getInstance().signOut()
        mGoogleSignInClient.signOut()


        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}