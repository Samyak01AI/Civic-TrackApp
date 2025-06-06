package com.example.civic_trackapplication

import NetworkMonitor
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.civic_trackapplication.databinding.ActivityLoginBinding
import com.example.civic_trackapplication.viewmodels.AuthViewModel
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
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.Locale

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private val viewModel: AuthViewModel by viewModels()
    private var timestamp: Timestamp? = null
    private lateinit var networkMonitor: NetworkMonitor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
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

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.btnFacebook1.setOnClickListener {
            binding.btnFacebook.performClick()
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val name = binding.nameInput.text.toString().trim()
            val pref = getSharedPreferences("user_pref", MODE_PRIVATE)
            timestamp = parseToTimestamp(pref.getString("joinDate", "").toString())

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val pref = getSharedPreferences("user_pref", MODE_PRIVATE)
                            val registeredEmail = pref.getString("email", "null")
                            val registeredPassword = pref.getString("password", "null")

                            if (email == registeredEmail && password == registeredPassword) {
                                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                                // Update SharedPreferences with name if not already present
                                pref.edit().apply {
                                    putString("name", name)
                                    putBoolean("isLoggedIn", true)
                                    apply()
                                }
                            }
                            val viewModel: AuthViewModel by viewModels()
                            viewModel.checkAdminStatus()
                            viewModel.isAdmin.observe(this) { isAdmin ->
                                if (isAdmin) {
                                    val intent = Intent(this, AdminActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        } else {
                            Toast.makeText(
                                this, "Login failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
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
                    Toast.makeText(this@LoginActivity, "Login cancelled", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        binding.btnGoogle.setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, 9001)
        }
        binding.signupRedirect.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
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
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mGoogleSignInClient.signOut().addOnCompleteListener {
        }
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    if (isNewUser) {
                        Toast.makeText(this, "Account doesn't exist. Please sign up", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        val pref = getSharedPreferences("user_pref", MODE_PRIVATE)
                        pref.edit().apply {
                            putBoolean("isLoggedIn", true)
                            apply()
                        }
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    if (isNewUser) {
                     Toast.makeText(this, "Account doesn't exist. Please sign up", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        val pref = getSharedPreferences("user_pref", MODE_PRIVATE)
                        pref.edit().apply {
                            putBoolean("isLoggedIn", true)
                            apply()
                        }
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
    fun parseToTimestamp(dateString: String): Timestamp? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = format.parse(dateString)
            if (date != null) Timestamp(date) else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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