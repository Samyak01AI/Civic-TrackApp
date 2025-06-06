package com.example.civic_trackapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.civic_trackapplication.Issue
import com.example.civic_trackapplication.Issue.Companion.formatTimestampToMonthDate
import com.example.civic_trackapplication.R
import com.example.civic_trackapplication.databinding.ItemProfileIssueBinding

class ProfileIssuesAdapter(
    private val onItemClick: (issue: Issue) -> Unit
) : ListAdapter<Issue, ProfileIssueViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileIssueViewHolder {
        val binding = ItemProfileIssueBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfileIssueViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ProfileIssueViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ProfileIssueViewHolder(
    private val binding: ItemProfileIssueBinding,
    private val onItemClick: (Issue) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(issue: Issue) {
        binding.apply {
            tvTitle.text = issue.title
            tvStatus.text = issue.status.replaceFirstChar { it.uppercase() }
            tvDate.text =  formatTimestampToMonthDate(issue.timestamp)

            val colorRes = when (issue.status) {
                "resolved" -> R.color.status_approved
                "in_progress" -> R.color.status_processing
                else -> R.color.status_pending
            }

            tvStatus.setTextColor(ContextCompat.getColor(root.context, colorRes))

            root.setOnClickListener {
                onItemClick(issue)
            }
        }
    }
}

class DiffCallback : DiffUtil.ItemCallback<Issue>() {
    override fun areItemsTheSame(oldItem: Issue, newItem: Issue) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Issue, newItem: Issue) =
        oldItem == newItem
}
