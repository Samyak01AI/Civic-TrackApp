package com.example.civictrackapplication

import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SimpleListAdapter(private val items: List<String>) :
    RecyclerView.Adapter<SimpleListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text.text = items[position]
    }
}