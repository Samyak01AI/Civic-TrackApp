package com.example.civic_trackapplication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    val mAuth = FirebaseAuth.getInstance()
    val user = FirebaseAuth.getInstance()
    val userId = user.currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Create an instance of the IssuesAndComments class to use its methods
        val o1 = IssuesAndComments()
        mAuth.signInWithEmailAndPassword("me.samyak06@gmail.com","123456")
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT).show()

                    o1.createOrCommentOnIssue("$userId", "title2", "description2", "status2", "Open", "Anonymous Comment")
                    Toast.makeText(this, "Issue raised successfully", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }

        supportFragmentManager.beginTransaction()
            .replace(R.id.main, Issue())
            .addToBackStack(null) // Optional: Adds this transaction to the back stack
            .commit()
    }
}