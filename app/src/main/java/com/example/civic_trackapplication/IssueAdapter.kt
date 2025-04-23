package com.example.civic_trackapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IssueAdapter(private val issues: List<String>) :
    RecyclerView.Adapter<IssueAdapter.ViewHolder>() {

    // ViewHolder class to hold UI elements
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIssue: TextView = view.findViewById(R.id.tvTitle)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val imgIssue: ImageView = view.findViewById(R.id.imgIssue)
    }

    // Inflate your custom layout (item_issue.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_issue, parent, false) // Use your XML, not the default
        return ViewHolder(view)
    }

    // Rest of the code remains the same
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvIssue.text = issues[position]
        holder.tvLocation.text = "Near Main Street"
        holder.tvStatus.text = "Pending"
        holder.imgIssue.setImageResource(R.drawable.ic_report)
    }

    override fun getItemCount() = issues.size
}