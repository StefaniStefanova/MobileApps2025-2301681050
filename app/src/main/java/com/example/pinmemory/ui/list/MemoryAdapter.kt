package com.example.pinmemory.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pinmemory.R
import com.example.pinmemory.data.local.MemoryEntity
import java.text.SimpleDateFormat
import java.util.*

class MemoryAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<MemoryEntity, MemoryAdapter.MemoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memory, parent, false)
        return MemoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(memory: MemoryEntity) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            itemView.findViewById<TextView>(R.id.tvItemTitle).text = memory.title
            itemView.findViewById<TextView>(R.id.tvItemLocation).text = " ${memory.locationName}"
            itemView.findViewById<TextView>(R.id.tvItemDate).text = dateFormat.format(Date(memory.date))

            val ivThumbnail = itemView.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.ivThumbnail)
            if (memory.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(memory.imageUrl)
                    .centerCrop()
                    .into(ivThumbnail)
            } else {
                ivThumbnail.setImageDrawable(null)
            }

            itemView.setOnClickListener { onItemClick(memory.id) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MemoryEntity>() {
        override fun areItemsTheSame(oldItem: MemoryEntity, newItem: MemoryEntity) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MemoryEntity, newItem: MemoryEntity) =
            oldItem == newItem
    }
}