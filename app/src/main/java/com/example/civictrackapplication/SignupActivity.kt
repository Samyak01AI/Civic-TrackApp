package com.example.civictrackapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.civic_trackapplication.databinding.ActivitySignupBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    val callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {

        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(application)

        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.signupButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val name = "User" // Or ask for name if you want a field

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            } else {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        val userId = firebaseAuth.currentUser?.uid!!
                        val user = hashMapOf("name" to name, "email" to email)
                        firestore.collection("users").document(userId).set(user)

                        Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java)
                            .putExtra("username", name))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Signup failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.loginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
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
                                    // Logged in
                                } else {
                                    // Error
                                }
                            }
                    }

                    override fun onCancel() {}
                    override fun onError(error: FacebookException) {}
                })
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

}
