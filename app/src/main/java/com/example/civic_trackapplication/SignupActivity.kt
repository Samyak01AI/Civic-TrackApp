package com.example.civic_trackapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.civic_trackapplication.databinding.ActivitySignupBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging


class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private lateinit var callbackManager: CallbackManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.setClientToken("ab992de730789fbf5f3997fa8553ed86")
            FacebookSdk.sdkInitialize(applicationContext)
            AppEventsLogger.activateApp(application)
        }

        callbackManager = CallbackManager.Factory.create()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnFacebook1.setOnClickListener {
            binding.btnFacebook.performClick()
        }
        binding.signupButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val name = binding.nameInput.text.toString().trim()
            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            } else {
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val pref = getSharedPreferences("user_pref", MODE_PRIVATE)
                            pref.edit().apply {
                                putString("name", name)
                                putString("email", email)
                                putString("password", password)
                                putString("joinDate", Timestamp.now().toString())
                                apply()
                            }
                        }

                            val user = firebaseAuth.currentUser
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()

                            user?.updateProfile(profileUpdates)
                                ?.addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        user.reload().addOnSuccessListener {
                                            val updatedName = user.displayName
                                            Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            if (user != null) {
                                val user1 = FirebaseAuth.getInstance().currentUser
                                user1?.let {
                                    val uid = user?.uid ?: "000"
                                    val name = name ?: "No Name"
                                    val email = email ?: "No Email"
                                    val photoUrl = user?.photoUrl?.toString() ?: ""

                                    val userData = hashMapOf(
                                        "uid" to uid,
                                        "name" to name,
                                        "email" to email,
                                        "photoUrl" to photoUrl,
                                        "joinDate" to Timestamp.now() // optional
                                    )

                                    val db = FirebaseFirestore.getInstance()
                                    db.collection("users").document(uid).set(userData)
                                        .addOnSuccessListener {
                                            Log.d("Firestore", "User data saved")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Firestore", "Failed to save user: ${e.message}")
                                        }
                                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val token = task.result
                                            FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(uid)
                                                .update("fcmToken", token)
                                            val intent = Intent(this, LoginActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Signup failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }

        }

        //Facebook Sign-In
        binding.btnFacebook.setPermissions("email", "public_profile")
        binding.btnFacebook.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    Toast.makeText(this@SignupActivity, "Login cancelled", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(
                        this@SignupActivity,
                        "Login error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })


        //Google Sign-In
            binding.btnGoogle.setOnClickListener {
                val signInIntent = mGoogleSignInClient.signInIntent
                startActivityForResult(signInIntent, 9001)
            }


        binding.loginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9001){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult< ApiException >(ApiException::class.java)
                firebaseAuthWithGoogle(account.getIdToken())
            } catch (e: ApiException) {
                e.printStackTrace()
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        mGoogleSignInClient.signOut().addOnCompleteListener { }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.let {
                        val sharedPref = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("name", it.displayName)
                            putString("email", it.email)
                            putString("uid", it.uid)
                            putString("user_photo", it.photoUrl?.toString())
                            putBoolean("isLoggedIn", true)
                            putString("joinDate", Timestamp.now().toString())
                            apply()
                        }
                    }
                    user?.let {
                        val uid = user.uid
                        val name = user.displayName ?: "No Name"
                        val email = user.email ?: "No Email"
                        val photoUrl = user.photoUrl?.toString() ?: ""

                        val userData = hashMapOf(
                            "uid" to uid,
                            "name" to name,
                            "email" to email,
                            "photoUrl" to photoUrl,
                            "joinDate" to Timestamp.now() // optional
                        )

                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(uid).set(userData)
                            .addOnSuccessListener {
                                Log.d("Firestore", "User data saved")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Failed to save user: ${e.message}")
                            }
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .update("fcmToken", token)
                            }
                        }
                    }
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Google sign-in failed.", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_SHORT).show()

                    user?.let {
                        val uid = user.uid
                        val name = user.displayName ?: "No Name"
                        val email = user.email ?: "No Email"
                        val photoUrl = user.photoUrl?.toString() ?: ""

                        val userData = hashMapOf(
                            "uid" to uid,
                            "name" to name,
                            "email" to email,
                            "photoUrl" to photoUrl,
                            "joinDate" to Timestamp.now() // optional
                        )

                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(uid).set(userData)
                            .addOnSuccessListener {
                                Log.d("Firestore", "User data saved")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Failed to save user: ${e.message}")
                            }
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .update("fcmToken", token)
                            }
                        }
                    }
                    val pref = getSharedPreferences("user_pref", MODE_PRIVATE)
                    pref.edit().apply {
                        putString("name", user?.displayName)
                        putString("email", user?.email)
                        putString("uid", user?.uid)
                        putString("user_photo", user?.photoUrl?.toString())
                        putBoolean("isLoggedIn", true)
                        putString("joinDate",Timestamp.now().toString())
                        apply()
                    }
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}