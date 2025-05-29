package com.example.civic_trackapplication.viewmodels

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.civic_trackapplication.Issue
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _userData = MutableLiveData<UserProfile>()
    val userData: LiveData<UserProfile> = _userData

    private val _userIssues = MutableLiveData<List<Issue>>()
    val userIssues: LiveData<List<Issue>> = _userIssues

    private val _isDarkMode = MutableLiveData<Boolean>()
    val isDarkMode: LiveData<Boolean> = _isDarkMode

    init {
        loadUserProfile()
        loadUserIssues()
        loadThemePreference()
    }

    private fun loadUserProfile() {
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.let {
                        _userData.value = UserProfile(
                            name = it.getString("name") ?: "Anonymous",
                            email = auth.currentUser?.email ?: "",
                            imageUrl = it.getString("imageUrl") ?: "",
                            joinDate = it.getTimestamp("joinDate")?.toDate()
                        )
                    }
                }
        }
    }

    private fun loadUserIssues() {
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("Issues")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    val issues = snapshot?.documents?.mapNotNull { doc ->
                        Issue.fromDocument(doc)
                    } ?: emptyList()
                    _userIssues.value = issues
                }
        }
    }

    fun fetchUserIssues() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("Issues")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null){
                    _userIssues.postValue(emptyList())
                }
                else {
                    val issues = snapshot.documents.mapNotNull {
                        Issue.fromDocument(it)
                    }
                    _userIssues.postValue(issues)
                }
            }
    }


    fun setDarkModeEnabled(enabled: Boolean) {
        _isDarkMode.value = enabled
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun loadThemePreference() {
    }

    fun logout() {
        auth.signOut()

    }


    data class UserProfile(
        val name: String,
        val email: String,
        val imageUrl: String,
        val joinDate: Date? = Date(),
    )
}