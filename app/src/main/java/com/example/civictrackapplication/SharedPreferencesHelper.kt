// Filename: SharedPreferencesHelper.kt
package com.example.civictrack

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("CivicPrefs", Context.MODE_PRIVATE)

    fun saveName(name: String) {
        prefs.edit().putString("name", name).apply()
    }

    fun saveEmail(email: String) {
        prefs.edit().putString("email", email).apply()
    }

    fun getName(): String {
        return prefs.getString("name", "Your Name") ?: "Your Name"
    }

    fun getEmail(): String {
        return prefs.getString("email", "you@example.com") ?: "you@example.com"
    }
}
