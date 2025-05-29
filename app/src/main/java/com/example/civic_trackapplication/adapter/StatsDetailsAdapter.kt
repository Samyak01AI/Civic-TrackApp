package com.example.civic_trackapplication.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.civic_trackapplication.databinding.ItemStatHeaderBinding
import com.example.civic_trackapplication.databinding.ItemStatValueBinding
import com.example.civic_trackapplication.viewmodels.AdminStats
import com.example.civic_trackapplication.viewmodels.AdminStatsViewModel

class StatsDetailsAdapter : ListAdapter<StatsDetailsAdapter.StatItem, StatsDetailsAdapter.BaseViewHolder>(DiffCallback()) {

    sealed class StatItem {
        data class Header(val title: String) : StatItem()
        data class Stat(val label: String, val value: String) : StatItem()
    }

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: StatItem)
    }

    class HeaderViewHolder(private val binding: ItemStatHeaderBinding) : BaseViewHolder(binding.root) {
        override fun bind(item: StatItem) {
            val header = item as StatItem.Header
            binding.tvHeader.text = header.title
        }
    }

    class StatItemViewHolder(private val binding: ItemStatValueBinding) : BaseViewHolder(binding.root) {
        override fun bind(item: StatItem) {
            val stat = item as StatItem.Stat
            binding.tvLabel.text = stat.label
            binding.tvValue.text = stat.value
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<StatItem>() {
        override fun areItemsTheSame(oldItem: StatItem, newItem: StatItem): Boolean {
            return when {
                oldItem is StatItem.Header && newItem is StatItem.Header ->
                    oldItem.title == newItem.title
                oldItem is StatItem.Stat && newItem is StatItem.Stat ->
                    oldItem.label == newItem.label && oldItem.value == newItem.value
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: StatItem, newItem: StatItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is StatItem.Header -> VIEW_TYPE_HEADER
            is StatItem.Stat -> VIEW_TYPE_STAT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                ItemStatHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            VIEW_TYPE_STAT -> StatItemViewHolder(
                ItemStatValueBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun submitStats(statsData: AdminStats) {
        val items = mutableListOf<StatItem>().apply {
            add(StatItem.Header("Status Distribution"))
            statsData.statusDistribution.forEach { (status, count) ->
                add(StatItem.Stat(status.replaceFirstChar { it.uppercase() }, count.toString()))
            }

            add(StatItem.Header("Weekly Trends"))
            statsData.weeklyTrends.forEach { (week, count) ->
                add(StatItem.Stat(week, count.toString()))
            }
        }
        submitList(items)
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_STAT = 1
    }
}