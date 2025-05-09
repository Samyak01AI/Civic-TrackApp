package com.example.civictrackapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.civic_trackapplication.R
import com.example.civictrackapplication.FAQAdapter
import com.example.civictrackapplication.FAQ

class HelpSupportActivity : AppCompatActivity() {

    private lateinit var recyclerFaqs: RecyclerView
    private lateinit var btnContactSupport: Button
    private lateinit var btnFeedback: Button

    private val faqs = listOf(
        FAQ("How to report an issue?", "Go to 'Report Issue', fill the form and submit."),
        FAQ("Can I track my complaints?", "Yes, check 'My Complaints' to view status."),
        FAQ("How do I contact support?", "Use the 'Contact Support' button below."),
        FAQ("What if I forget my password?", "Click 'Forgot Password' on the login screen."),
        FAQ("How to update profile info?", "Go to 'My Account' and update your details.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_support)

        recyclerFaqs = findViewById(R.id.recyclerFaqs)
        btnContactSupport = findViewById(R.id.btnContactSupport)
        btnFeedback = findViewById(R.id.btnFeedback)

        setupFaqRecycler()
        setupListeners()
    }

    private fun setupFaqRecycler() {
        recyclerFaqs.layoutManager = LinearLayoutManager(this)
        recyclerFaqs.adapter = FAQAdapter(faqs)
    }

    private fun setupListeners() {
        btnContactSupport.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@civictrack.com")
                putExtra(Intent.EXTRA_SUBJECT, "CivicTrack App Support")
            }
            startActivity(Intent.createChooser(emailIntent, "Send Email"))
        }

        btnFeedback.setOnClickListener {
            showFeedbackDialog()
        }
    }

    private fun showFeedbackDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_feedback, null)
        val edtFeedback = dialogView.findViewById<EditText>(R.id.edtFeedback)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Send Feedback")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val feedback = edtFeedback.text.toString().trim()
                if (feedback.isNotEmpty()) {
                    Toast.makeText(this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter feedback", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        // Set custom dialog size after showing
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.98).toInt(),  // 90% of screen width
            (resources.displayMetrics.heightPixels * 0.60).toInt()  // 50% of screen height
        )
    }

}

