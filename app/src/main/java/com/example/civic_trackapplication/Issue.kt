package com.example.civictrack.models

data class Issue(
    val title: String = "",
    val location: String = "",
    val status: String = "",
    val category: String = "",
    val submittedBy: String = "",
    val imageRes: Int
)
