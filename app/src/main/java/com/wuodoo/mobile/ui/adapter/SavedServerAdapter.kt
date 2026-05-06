package com.wuodoo.mobile.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wuodoo.mobile.data.ServerInfo
import com.wuodoo.mobile.databinding.ItemSavedServerBinding

class SavedServerAdapter(
    private val servers: List<ServerInfo>,
    private val onItemClick: (ServerInfo) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<SavedServerAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemSavedServerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(server: ServerInfo, position: Int) {
            binding.tvServerUrl.text = "${server.protocol}://${server.host}"
            binding.tvDatabase.text  = if (server.database.isNotEmpty()) "DB: ${server.database}" else ""
            binding.root.setOnClickListener { onItemClick(server) }
            binding.btnDelete.setOnClickListener { onDeleteClick(position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSavedServerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(servers[position], position)
    }

    override fun getItemCount() = servers.size
}
