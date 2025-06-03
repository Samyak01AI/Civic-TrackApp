package com.example.civic_trackapplication.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.civic_trackapplication.Comment
import com.example.civic_trackapplication.Issue
import com.example.civic_trackapplication.R
import com.example.civic_trackapplication.databinding.ItemAdminIssuesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminIssuesAdapter(
    private val onStatusChanged: (issueId: String, newStatus: String) -> Unit,
    private val onItemClick: (issue: Issue) -> Unit
) : RecyclerView.Adapter<AdminIssuesAdapter.IssueViewHolder>() {

    private var issues = mutableListOf<Issue>()
    inner class IssueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding: ItemAdminIssuesBinding = ItemAdminIssuesBinding.bind(itemView)

        fun bind(issue: Issue) {
            with(binding) {
                tvTitle.text = issue.title
                tvLocation.text = issue.location
                val status = issue.status
                Log.d("status", status)
                if (status == "Pending") {
                    progressBar.progress = 5
                }
                if (status == "In Progress") {
                    progressBar.progress = 50
                }
                if (status == "Resolved") {
                    progressBar.progress = 100
                }
                val priority = issue.priorityScore
                Log.d("priority", priority.toString())
                if (priority < 5 && priority >= 0) {
                    tvStatus.text = "Low Priority"
                    tvStatus.setTextColor(Color.BLACK)
                    tvStatus.setBackgroundColor(Color.GREEN)
                } else if (priority < 8 && priority >= 5) {
                    tvStatus.text = "Medium Priority"
                    tvStatus.setTextColor(Color.BLACK)
                    tvStatus.setBackgroundColor(Color.YELLOW)
                } else if (priority <= 8) {
                    tvStatus.text = "High Priority"
                    tvStatus.setBackgroundColor(Color.RED)
                }

                radioStatusGroup.setOnCheckedChangeListener(null)
                when (issue.status) {
                    "Pending" -> radioPending.isChecked = true
                    "In Progress" -> radioInProgress.isChecked = true
                    "Resolved" -> radioResolved.isChecked = true
                    else -> radioPending.isChecked = true
                }

                radioStatusGroup.setOnCheckedChangeListener { _, checkedId ->
                    val newStatus = when (checkedId) {
                        R.id.radioPending -> "Pending"
                        R.id.radioInProgress -> "In Progress"
                        R.id.radioResolved -> "Resolved"
                        else -> radioPending.text.toString()
                    }
                    if (newStatus != issue.status) {
                        onStatusChanged(issue.id, newStatus)
                    }
                }
                root.setOnClickListener {
                    onItemClick(issue)  // Navigate to details
                }
            }

            binding.commentInput.setOnClickListener {
                showCommentDialog(issue.id)
            }
        }
        fun showCommentDialog(issueId: String) {
            val dialogView = LayoutInflater.from(itemView.context).inflate(R.layout.dialog_comments, null)
            val commentAdapter = CommentsAdapter()
            val rvComments = dialogView.findViewById<RecyclerView>(R.id.rvComments)
            val etComment = dialogView.findViewById<EditText>(R.id.etComment)
            val btnPost = dialogView.findViewById<Button>(R.id.btnPost)

            rvComments.layoutManager = LinearLayoutManager(itemView.context)
            rvComments.adapter = commentAdapter

            val commentRef = FirebaseFirestore.getInstance()
                .collection("Issues").document(issueId)
                .collection("comments").orderBy("timestamp")

            val listener = commentRef.addSnapshotListener { snapshot, _ ->
                val comments = snapshot?.toObjects(Comment::class.java) ?: emptyList()
                commentAdapter.submitList(comments)
            }

            val alertDialog = AlertDialog.Builder(itemView.context)
                .setView(dialogView)
                .setOnDismissListener { listener.remove() }
                .create()

            btnPost.setOnClickListener {
                val commentText = etComment.text.toString().trim()
                if (commentText.isNotEmpty()) {
                    val user = FirebaseAuth.getInstance().currentUser
                    val comment = Comment(
                        userId = user?.uid ?: "",
                        userName = user?.displayName ?: "Anonymous",
                        comment = commentText,
                        timestamp = System.currentTimeMillis()
                    )
                    FirebaseFirestore.getInstance()
                        .collection("Issues").document(issueId)
                        .collection("comments")
                        .add(comment)
                    etComment.setText("")
                }
            }

            alertDialog.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_issues, parent, false)
        return IssueViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: IssueViewHolder,
        position: Int
    ) {
        holder.bind(issues[position])
    }


    override fun getItemCount(): Int = issues.size

    fun submitList(newList: List<Issue>) {
        issues.clear()
        issues.addAll(newList)
        notifyDataSetChanged()
    }
}