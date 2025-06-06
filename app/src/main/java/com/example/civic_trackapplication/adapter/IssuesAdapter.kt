package com.example.civic_trackapplication.adapter

import android.graphics.BlendMode
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColor
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.civic_trackapplication.Comment
import com.example.civic_trackapplication.Issue
import com.example.civic_trackapplication.Issue.Companion.formatTimestampToMonthDate
import com.example.civic_trackapplication.R
import com.example.civic_trackapplication.databinding.ItemIssueBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.text.DateFormat

class IssuesAdapter(
    private val onItemClick: (issue: Issue) -> Unit
) : ListAdapter<Issue, IssuesAdapter.IssueViewHolder>(DiffCallback()) {
    private var issues = mutableListOf<Issue>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val binding = ItemIssueBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IssueViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    override fun getItemCount(): Int = issues.size

    fun updateList(newList: List<Issue>) {
        issues.clear()
        issues.addAll(newList)
        notifyDataSetChanged()
    }
    inner class IssueViewHolder(
        private val binding: ItemIssueBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(issue: Issue) {
            FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

            binding.apply {
                tvTitle.text = issue.title
                tvLocation.text = issue.location
                tvDate.text =  formatTimestampToMonthDate(issue.timestamp)
                val status = issue.status
                tvStatus.text = status
                Log.d("status", status)
                if (status == "Pending" || status == "pending") {
                    progressBar.progress = 5
                    tvStatus.setTextColor(Color.BLACK)
                    tvStatus.setBackgroundColor(Color.YELLOW)
                }
                else if (status == "In Progress") {
                    progressBar.progress = 50
                    tvStatus.setTextColor(Color.WHITE)
                    tvStatus.setBackgroundColor(Color.BLUE)
                }
                else if (status == "Resolved") {
                    progressBar.progress = 100
                    tvStatus.setTextColor(Color.BLACK)
                    tvStatus.setBackgroundColor(Color.GREEN)
                }
                val priority = issue.priorityScore
                if(priority<5 && priority>=0){
                    tvPriority.text = "Low Priority"
                    tvPriority.setTextColor(Color.BLACK)
                    tvPriority.setBackgroundColor(Color.GREEN)
                }
                else if(priority < 8 && priority >= 5){
                    tvPriority.text = "Medium Priority"
                    tvPriority.setTextColor(Color.BLACK)
                    tvPriority.setBackgroundColor(Color.YELLOW)
                }
                else if(priority <= 8){
                    tvPriority.text = "High Priority"
                    tvPriority.setBackgroundColor(Color.RED)
                }

                // Load image with Glide
                Glide.with(root.context)
                    .load(issue.imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(imgIssue)

                root.setOnClickListener {
                    onItemClick(issue)
                }
            }

            binding.upvoteBtn.setOnClickListener {
                val context = itemView.context
                val db = FirebaseFirestore.getInstance()
                val issueRef = db.collection("Issues").document(issue.id)
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                binding.upvoteLoading.visibility = View.VISIBLE
                binding.upvoteLoading.playAnimation()
                binding.upvoteLoading.speed = 0.5f
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(issueRef)
                    val upvotedBy = snapshot.get("upvotedBy") as? Map<String, Boolean> ?: emptyMap()
                    if (upvotedBy.containsKey(userId)) {
                        binding.upvoteBtn.setColorFilter(ContextCompat.getColor(itemView.context, R.color.text_primary))
                        throw FirebaseFirestoreException(
                            "Already upvoted",
                            FirebaseFirestoreException.Code.ABORTED
                        )
                    }

                    val newUpvotes = snapshot.getLong("upvotes")?.plus(1) ?: 1
                    val updatedUpvotedBy = upvotedBy.toMutableMap()
                    updatedUpvotedBy[userId] = true

                    transaction.update(issueRef, mapOf(
                        "upvotes" to newUpvotes,
                        "upvotedBy" to updatedUpvotedBy
                    ))
                }.addOnSuccessListener {
                    binding.upvoteBtn.setColorFilter(ContextCompat.getColor(itemView.context, R.color.text_primary))
                }.addOnFailureListener { e ->
                    if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.ABORTED) {
                        Toast.makeText(context, "You have already upvoted this issue.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to upvote", Toast.LENGTH_SHORT).show()
                    }
                }.addOnCompleteListener {
                    binding.upvoteLoading.cancelAnimation()
                    binding.upvoteLoading.visibility = View.GONE
                }
            }
            val upvoteCount = issue.upvotedBy.size
            binding.tvUpvoteCount.text = upvoteCount.toString()

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

    class DiffCallback : DiffUtil.ItemCallback<Issue>() {
        override fun areItemsTheSame(oldItem: Issue, newItem: Issue): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Issue, newItem: Issue): Boolean {
            return oldItem == newItem
        }
    }
}