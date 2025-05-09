package com.example.civictrackapplication


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.civic_trackapplication.R

class FAQAdapter(private val faqs: List<FAQ>) :
    RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {

    inner class FAQViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val question: TextView = itemView.findViewById(R.id.faq_question)
        val answer: TextView = itemView.findViewById(R.id.faq_answer)

        init {
            itemView.setOnClickListener {
                answer.visibility = if (answer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.faq_item, parent, false)
        return FAQViewHolder(view)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        val faq = faqs[position]
        holder.question.text = faq.question
        holder.answer.text = faq.answer
    }

    override fun getItemCount(): Int = faqs.size
}
