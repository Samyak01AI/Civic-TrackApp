package com.example.civic_trackapplication

data class Issue(
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val status: String = "",
    val submittedBy: String = "",
    val imageUrl: String = "",
    val timestamp: String = "",
    val location: Any? = null,
    val comments: Comment = Comment()
) {
    constructor() : this("", "", "", "", "", "", "", null, Comment())
}

data class Comment(
    val text: String = "",
    val time: String = "",
    val userId: String = ""
)
