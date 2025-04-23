package com.example.civic_trackapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.civic_trackapplication.R

class IssueAdapter(private val issues: List<String>) :
    RecyclerView.Adapter<IssueAdapter.ViewHolder>() {

    // ViewHolder class to hold UI elements
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIssue: TextView = view.findViewById(R.id.tvIssueName) // Match XML ID
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
    }

    override fun getItemCount() = issues.size
}