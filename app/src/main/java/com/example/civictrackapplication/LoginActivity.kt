package com.example.civictrackapplication

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.civic_trackapplication.databinding.ActivityLoginBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val callbackManager = CallbackManager.Factory.create()

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Now call after setContentView
        printKeyHash()

        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(application)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val userId = firebaseAuth.currentUser?.uid!!
                    firestore.collection("users").document(userId).get()
                        .addOnSuccessListener { doc ->
                            val name = doc.getString("name") ?: "User"
                            startActivity(Intent(this, HomeActivity::class.java)
                                .putExtra("username", name))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to load user", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Login failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }

        binding.signupRedirect.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.imgFacebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        val credential = FacebookAuthProvider.getCredential(loginResult.accessToken.token)
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this@LoginActivity, "Facebook login successful", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this@LoginActivity, "Facebook login failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }

                    override fun onCancel() {
                        Toast.makeText(this@LoginActivity, "Facebook login cancelled", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(error: FacebookException) {
                        Toast.makeText(this@LoginActivity, "Facebook error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun printKeyHash() {
        try {
            val info: PackageInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )

            for (signature in info.signingInfo?.apkContentsSigners!!) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT)

                // Show in logcat and toast
                Log.d("KeyHash", "KeyHash: $keyHash")
                Toast.makeText(this, "KeyHash:\n$keyHash", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "KeyHash error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}
