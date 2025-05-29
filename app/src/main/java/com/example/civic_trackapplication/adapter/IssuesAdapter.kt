package com.example.civic_trackapplication.adapter

import android.graphics.BlendMode
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.civic_trackapplication.Issue
import com.example.civic_trackapplication.Issue.Companion.formatTimestampToMonthDate
import com.example.civic_trackapplication.R
import com.example.civic_trackapplication.databinding.ItemIssueBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DateFormat

class IssuesAdapter(
    private val onItemClick: (Issue) -> Unit
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
            binding.apply {
                tvTitle.text = issue.title
                tvLocation.text = issue.location
                tvDate.text =  formatTimestampToMonthDate(issue.timestamp)
                val status = issue.status
                tvStatus.text = status
                Log.d("status", status)
                if (status == "Pending") {
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
                    tvPriority.setBackgroundColor(Color.GREEN)
                }
                else if(priority < 8 && priority >= 5){
                    tvPriority.text = "Medium Priority"
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

                root.setOnClickListener { onItemClick(issue) }
            }
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