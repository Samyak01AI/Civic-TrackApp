package com.example.civic_trackapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ComplaintAdapter(private val complaints: List<String>) :
    RecyclerView.Adapter<ComplaintAdapter.ViewHolder>() {

    // ViewHolder class to hold list items
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvComplaint: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvComplaint.text = complaints[position]
    }

    override fun getItemCount(): Int = complaints.size
}