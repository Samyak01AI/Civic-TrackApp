package com.example.civic_trackapplication.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.civic_trackapplication.Issue
import com.example.civic_trackapplication.R
import com.example.civic_trackapplication.databinding.ItemAdminIssueBinding

class AdminIssuesAdapter(
    private val onStatusChanged: (Issue, String) -> Unit
) : ListAdapter<Issue, AdminIssuesAdapter.ViewHolder>(DiffCallback()) {
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

    inner class ViewHolder(private val binding: ItemAdminIssueBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(issue: Issue) {
            binding.apply {
                tvTitle.text = issue.title
                tvUser.text = "Submitted by: ${issue.userId.take(8)}..." // Or fetch username
                tvDate.text = issue.timestamp.toDate().toString()

                // Status spinner
                ArrayAdapter.createFromResource(
                    root.context,
                    R.array.issue_statuses,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerStatus.adapter = adapter
                    spinnerStatus.setSelection(
                        adapter.getPosition(issue.status.replaceFirstChar { it.uppercase() })
                    )
                }

                spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, id: Long) {
                        val newStatus = root.context.resources.getStringArray(R.array.issue_statuses)[pos]
                        if (newStatus != issue.status) {
                            onStatusChanged(issue, newStatus)
                        }
                    }
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                }

                // Load image with Glide
                Glide.with(root)
                    .load(issue.imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(ivImage)
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