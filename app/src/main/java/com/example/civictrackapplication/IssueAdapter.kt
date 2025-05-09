package com.example.civictrackapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.civic_trackapplication.R

class IssueAdapter(private var issues: List<Issue>) :
    RecyclerView.Adapter<IssueAdapter.IssueViewHolder>() {

    private val colors = listOf("#FFE0B2", "#C8E6C9", "#FFCDD2", "#D1C4E9", "#BBDEFB")

    inner class IssueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.imgIssue)
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val location: TextView = itemView.findViewById(R.id.tvLocation)
        val status: TextView = itemView.findViewById(R.id.tvStatus)
        val card: CardView = itemView.findViewById(R.id.cardRoot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_issue, parent, false)
        return IssueViewHolder(view)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        val issue = issues[position]
        holder.title.text = issue.title
        holder.location.text = issue.location.toString()
        holder.status.text = issue.status
        holder.img.setImageResource(R.drawable.ic_report) // fallback image

        // Apply random background color to card
        val color = Color.parseColor(colors[position % colors.size])
        holder.card.setCardBackgroundColor(color)
    }

    override fun getItemCount(): Int = issues.size

    fun filterByCategory(category: String) {
        issues = if (category == "All") originalList else originalList.filter { it.category == category }
        notifyDataSetChanged()
    }

    private var originalList: List<Issue> = issues

}
