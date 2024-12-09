package com.healbe.complex_example.connect.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.healbe.complex_example.R
import com.healbe.complex_example.connect.fragments.SearchAdapter.SearchViewHolder
import com.healbe.complex_example.databinding.RecyclerSearchItemBinding
import com.healbe.healbesdk.business_api.user_storage.entity.HealbeDevice

internal class SearchAdapter(private val listener: View.OnClickListener) :
    RecyclerView.Adapter<SearchViewHolder>() {
    private var devices = mutableListOf<HealbeDevice>()

    var saved: HealbeDevice? = null
        set(value) {
            field = value
            if (value != null) {
                val position = devices.indexOfFirst { it.isSaved(value) }
                if (position >= 0) {
                    notifyItemChanged(position)
                }
            }
        }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RecyclerSearchItemBinding.inflate(layoutInflater, parent, false)
        return SearchViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val device = devices.getOrNull(position) ?: return
        holder.bind(device, device.isSaved(saved))
    }

    override fun getItemCount(): Int = devices.size

    override fun getItemId(position: Int): Long =
        devices.getOrNull(position)?.mac?.hashCode()?.toLong() ?: RecyclerView.NO_ID

    fun add(device: HealbeDevice) {
        if (!devices.contains(device)) {
            devices.add(device)
            notifyItemInserted(devices.size - 1)
        }
    }

    fun clear() {
        if (devices.isNotEmpty()) {
            val count = devices.size
            devices.clear()
            notifyItemRangeRemoved(0, count)
        }
    }

    fun isEmpty() = devices.isEmpty()

    private fun HealbeDevice.isSaved(saved: HealbeDevice?) = mac == saved?.mac

    internal class SearchViewHolder(
        private val binding: RecyclerSearchItemBinding,
        listener: View.OnClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener(listener)
        }

        fun bind(device: HealbeDevice, isSaved: Boolean) {
            binding.apply {
                name.text = device.name
                if (isSaved) {
                    ind.setColorFilter(
                        ContextCompat.getColor(itemView.context, com.healbe.examples.common.R.color.main_purple)
                    )
                } else {
                    ind.colorFilter = null
                }
                root.tag = device
            }
        }
    }
}