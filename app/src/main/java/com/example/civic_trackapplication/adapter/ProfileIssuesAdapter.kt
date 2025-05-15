package com.example.civic_trackapplication.adapter

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.civic_trackapplication.Issue
import com.example.civic_trackapplication.R
import com.example.civic_trackapplication.databinding.ItemProfileIssueBinding

class ProfileIssuesAdapter(
    private val onItemClick: (Issue) -> Unit
) : ListAdapter<Issue, ProfileIssuesAdapter.ViewHolder>(DiffCallback()) {
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

    inner class ViewHolder(private val binding: ItemProfileIssueBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(issue: Issue) {
            binding.apply {
                tvTitle.text = issue.title
                tvStatus.text = issue.status.replaceFirstChar { it.uppercase() }
                tvDate.text = issue.timestamp.toDate().toString()

                // Status color coding
                val colorRes = when (issue.status) {
                    "resolved" -> R.color.status_resolved
                    "in_progress" -> R.color.status_in_progress
                    else -> R.color.status_pending
                }

                tvStatus.setTextColor(ContextCompat.getColor(root.context, colorRes))
               // ivStatus.setImageResource(iconRes)

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