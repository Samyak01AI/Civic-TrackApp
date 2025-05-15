package com.example.civic_trackapplication.adapter

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.civic_trackapplication.Issue
import com.example.civic_trackapplication.R
import com.example.civic_trackapplication.databinding.ItemStatusBinding

class StatusAdapter(
    private val onItemClick: (Issue) -> Unit
) : ListAdapter<Issue, StatusAdapter.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        TODO("Not yet implemented")
    }

    inner class ViewHolder(private val binding: ItemStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(issue: Issue) {
            binding.apply {
                tvTitle.text = issue.title
                tvId.text =  "${issue.location.latitude}, ${issue.location.longitude}"
                tvDate.text = issue.timestamp.toDate().toString()

                // Status indicator
                val (statusText, statusColor) = when (issue.status) {
                    "resolved" -> "Resolved" to R.color.status_resolved
                    "in_progress" -> "In Progress" to R.color.status_in_progress
                    else -> "Pending" to R.color.status_pending
                }

                tvStatus.text = statusText
                tvStatus.setTextColor(ContextCompat.getColor(root.context, statusColor))

                // Load image with Glide
                Glide.with(root)
                    .load(issue.imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(ivThumbnail)

                root.setOnClickListener { onItemClick(issue) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Issue>() {
        override fun areItemsTheSame(oldItem: Issue, newItem: Issue) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Issue, newItem: Issue) =
            oldItem == newItem
    }
}