package com.autoattendance.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.autoattendance.databinding.ItemLocationBinding
import com.autoattendance.model.OfficeLocation

class LocationAdapter(
    private val onDelete: (OfficeLocation) -> Unit,
    private val onToggle: (OfficeLocation) -> Unit,
    private val onCheckIn: (OfficeLocation) -> Unit,
    private val onCheckOut: (OfficeLocation) -> Unit
) : ListAdapter<OfficeLocation, LocationAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemLocationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(location: OfficeLocation) {
            binding.tvLocationName.text = location.name
            binding.tvCoords.text = "%.4f, %.4f".format(location.latitude, location.longitude)
            binding.tvRadius.text = "${location.radiusMeters.toInt()} m radius"

            binding.switchActive.isChecked = location.isActive
            binding.switchActive.setOnCheckedChangeListener(null)
            binding.switchActive.setOnCheckedChangeListener { _, _ -> onToggle(location) }

            binding.btnDelete.setOnClickListener { onDelete(location) }
            binding.btnManualCheckIn.setOnClickListener { onCheckIn(location) }
            binding.btnManualCheckOut.setOnClickListener { onCheckOut(location) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<OfficeLocation>() {
        override fun areItemsTheSame(oldItem: OfficeLocation, newItem: OfficeLocation) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: OfficeLocation, newItem: OfficeLocation) =
            oldItem == newItem
    }
}
