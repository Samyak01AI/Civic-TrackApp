package com.example.civic_trackapplication.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.civic_trackapplication.Issue
import com.example.civic_trackapplication.R
import com.example.civic_trackapplication.databinding.ItemAdminIssuesBinding
import com.google.firebase.firestore.FirebaseFirestore

class AdminIssuesAdapter(
    private val onStatusChanged: (issueId: String, newStatus: String) -> Unit
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
                if(priority<5 && priority>=0){
                    tvStatus.text = "Low Priority"
                    tvStatus.setBackgroundColor(Color.GREEN)
                }
                else if(priority < 8 && priority >= 5){
                    tvStatus.text = "Medium Priority"
                    tvStatus.setBackgroundColor(Color.YELLOW)
                }
                else if(priority <= 8){
                    tvStatus.text = "High Priority"
                    tvStatus.setBackgroundColor(Color.RED)
                }

                radioStatusGroup.setOnCheckedChangeListener(null)
                when(issue.status) {
                    "Pending" -> radioPending.isChecked = true
                    "In Progress" -> radioInProgress.isChecked = true
                    "Resolved" -> radioResolved.isChecked = true
                }

                radioStatusGroup.setOnCheckedChangeListener { _, checkedId ->
                    val newStatus = when(checkedId) {
                        R.id.radioPending -> "Pending"
                        R.id.radioInProgress -> "In Progress"
                        R.id.radioResolved -> "Resolved"
                        else -> issue.status
                    }
                    if (newStatus != issue.status) {
                        onStatusChanged(issue.id, newStatus)
                    }
                }
            }
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