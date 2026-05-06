package com.wuodoo.mobile.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wuodoo.mobile.databinding.ItemDatabaseBinding

class DatabaseAdapter(
    private val databases: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<DatabaseAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemDatabaseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dbName: String) {
            binding.tvDbName.text = dbName
            binding.root.setOnClickListener { onItemClick(dbName) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDatabaseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(databases[position])
    }

    override fun getItemCount() = databases.size
}
