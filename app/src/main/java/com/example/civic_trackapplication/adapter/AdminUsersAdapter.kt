package com.example.civic_trackapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.civic_trackapplication.R
import com.example.civic_trackapplication.viewmodels.AdminUser
import com.example.civic_trackapplication.databinding.ItemAdminUserBinding
class AdminUsersAdapter(
    private val onAdminStatusChanged: (userId: String, isAdmin: Boolean) -> Unit
) : RecyclerView.Adapter<AdminUsersAdapter.UserViewHolder>() {

    private var users = emptyList<AdminUser>()
    private val pendingUpdates = mutableMapOf<String, Boolean>()

    class UserViewHolder(
        private val binding: ItemAdminUserBinding,
        private val onStatusChanged: (String, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentUser: AdminUser? = null

        init {
            binding.switchAdmin.setOnCheckedChangeListener { _, isChecked ->
                currentUser?.let { user ->
                    onStatusChanged(user.uid, isChecked)
                }
            }
        }

        fun bind(user: AdminUser) {
            currentUser = user
            binding.apply {
                tvEmail.text = user.email
                tvUserId.text = "ID: ${user.uid.take(8)}..."
                switchAdmin.isChecked = user.isAdmin
                progressBar.visibility = View.GONE // Hide by default
                Glide.with(itemView)
                    .load(user.photoUrl)
                    .placeholder(R.drawable.profile)
                    .into(ivAvatar)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemAdminUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding, onAdminStatusChanged)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    fun submitList(newList: List<AdminUser>) {
        users = newList
        notifyDataSetChanged()
    }

    fun clearPendingUpdates() {
        pendingUpdates.clear()
        notifyDataSetChanged()
    }
}