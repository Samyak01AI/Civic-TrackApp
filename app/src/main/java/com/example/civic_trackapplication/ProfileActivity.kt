
package com.example.civic_trackapplication
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnEditProfile: Button

    private var name: String = ""
    private var email: String = ""

    companion object {
        private const val REQUEST_CODE_GALLERY = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        imgProfile = findViewById(R.id.imgProfile)
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        btnEditProfile = findViewById(R.id.btnEditProfile)

        loadProfile()

        btnEditProfile.setOnClickListener {
            showEditDialog()
        }

        imgProfile.setOnClickListener {
            showImageOptionDialog()
        }
    }

    private fun loadProfile() {
        val pref = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        name = pref.getString("name", "No Name") ?: "No Name"
        email = pref.getString("email", "No Email") ?: "No Email"
        val imageUriString = pref.getString("profile_image", null)

        tvName.text = name
        tvEmail.text = email

        if (imageUriString != null) {
            val imageFile = File(Uri.parse(imageUriString).path ?: "")
            if (imageFile.exists()) {
                imgProfile.setImageURI(Uri.fromFile(imageFile))
            } else {
                imgProfile.setImageResource(R.drawable.ic_profile) // fallback
            }
        } else {
            imgProfile.setImageResource(R.drawable.ic_profile)
        }
    }


    private fun showEditDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etName = view.findViewById<EditText>(R.id.etEditName)
        val etEmail = view.findViewById<EditText>(R.id.etEditEmail)

        etName.setText(name)
        etEmail.setText(email)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        view.findViewById<Button>(R.id.btnConfirmEdit).setOnClickListener {
            confirmSave(etName.text.toString(), etEmail.text.toString())
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.btnCancelEdit).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun confirmSave(newName: String, newEmail: String) {
        val view = layoutInflater.inflate(R.layout.dialog_confirm_changes, null)

        val alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        view.findViewById<Button>(R.id.btnYes).setOnClickListener {
            saveChanges(newName, newEmail)
            alertDialog.dismiss()
        }

        view.findViewById<Button>(R.id.btnNo).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun saveChanges(newName: String, newEmail: String) {
        val pref = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        pref.edit().apply {
            putString("name", newName)
            putString("email", newEmail)
            apply()
        }
        loadProfile()
        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun showImageOptionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Set Profile Picture")
        builder.setItems(arrayOf("Select Default Image", "Choose from Gallery")) { dialog, which ->
            when (which) {
                0 -> showDefaultImagesDialog()
                1 -> openGallery()
            }
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun showDefaultImagesDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_default_images, null)
        val img1 = view.findViewById<ImageView>(R.id.imgOption1)
        val img2 = view.findViewById<ImageView>(R.id.imgOption2)
        val img3 = view.findViewById<ImageView>(R.id.imgOption3)
        val img4 = view.findViewById<ImageView>(R.id.imgOption4)
        val img5 = view.findViewById<ImageView>(R.id.imgOption5)
        val img6 = view.findViewById<ImageView>(R.id.imgOption6)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        val clickListener = { resId: Int ->
            imgProfile.setImageResource(resId)
            val pref = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            pref.edit().remove("profile_image").apply() // Remove URI if any
            dialog.dismiss()
        }

        img1.setOnClickListener { clickListener(R.drawable.dp1) }
        img2.setOnClickListener { clickListener(R.drawable.dp2) }
        img3.setOnClickListener { clickListener(R.drawable.dp3) }
        img4.setOnClickListener { clickListener(R.drawable.dp4) }
        img5.setOnClickListener { clickListener(R.drawable.dp5) }
        img6.setOnClickListener { clickListener(R.drawable.dp6) }

        dialog.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            if (selectedImageUri != null) {
                try {
                    // ✅ Open an InputStream from selected URI
                    val inputStream = contentResolver.openInputStream(selectedImageUri)
                    val file = File(cacheDir, "profile_image.jpg")
                    val outputStream = FileOutputStream(file)

                    inputStream?.copyTo(outputStream)

                    inputStream?.close()
                    outputStream.close()

                    val savedImageUri = Uri.fromFile(file)

                    imgProfile.setImageURI(savedImageUri)

                    val pref = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
                    pref.edit().putString("profile_image", savedImageUri.toString()).apply()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
