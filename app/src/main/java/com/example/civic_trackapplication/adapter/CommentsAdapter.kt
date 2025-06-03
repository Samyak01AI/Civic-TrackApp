package com.example.civic_trackapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.civic_trackapplication.Comment
import com.example.civic_trackapplication.R
import com.example.civic_trackapplication.databinding.ItemCommentBinding

class CommentsAdapter : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {
    private val comments = mutableListOf<Comment>()

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemCommentBinding.bind(view)
        fun bind(comment: Comment) {
            binding.tvUserName.text = comment.userName
            binding.tvComment.text = comment.comment
            binding.tvTime.text = getTimeAgo(comment.timestamp)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CommentViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
    )

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount() = comments.size

    fun submitList(newList: List<Comment>) {
        comments.clear()
        comments.addAll(newList)
        notifyDataSetChanged()
    }
    fun getTimeAgo(time: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hr ago"
            else -> "$days day${if (days > 1) "s" else ""} ago"
        }
    }

}
