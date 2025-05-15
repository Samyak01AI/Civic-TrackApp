package com.example.civic_trackapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.civic_trackapplication.Issue
import com.example.civic_trackapplication.R
import com.example.civic_trackapplication.databinding.ItemStatusIssueBinding

class StatusIssuesAdapter(
    private val onItemClick: (Issue) -> Unit
) : ListAdapter<Issue, StatusIssuesAdapter.StatusIssueViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusIssueViewHolder {
        val binding = ItemStatusIssueBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StatusIssueViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: StatusIssueViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StatusIssueViewHolder(
        private val binding: ItemStatusIssueBinding,
        private val onItemClick: (Issue) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(issue: Issue) {
            binding.apply {
                tvTitle.text = issue.title
                tvLocation.text = issue.location.toString()
                tvDate.text = issue.timestamp.toDate().toString()

                // Status chip
                chipStatus.text = issue.status.replaceFirstChar { it.uppercase() }
                chipStatus.setChipBackgroundColorResource(
                    when (issue.status) {
                        "resolved" -> R.color.status_resolved
                        "in_progress" -> R.color.status_in_progress
                        else -> R.color.status_pending
                    }
                )

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