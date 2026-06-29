package com.autoattendance.ui.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.autoattendance.databinding.ItemAttendanceBinding
import com.autoattendance.model.AttendanceRecord
import java.text.SimpleDateFormat
import java.util.*

class AttendanceAdapter : ListAdapter<AttendanceRecord, AttendanceAdapter.ViewHolder>(DiffCallback()) {

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    inner class ViewHolder(private val binding: ItemAttendanceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: AttendanceRecord) {
            binding.tvEmployeeName.text = record.employeeName
            binding.tvEmployeeId.text = record.employeeId
            binding.tvLocation.text = record.locationName
            binding.tvCheckIn.text = record.checkInTime?.let { timeFormat.format(Date(it)) } ?: "—"
            binding.tvCheckOut.text = record.checkOutTime?.let { timeFormat.format(Date(it)) } ?: "Ongoing"
            binding.tvCoords.text = "%.4f, %.4f".format(record.latitude, record.longitude)

            val status = if (record.checkOutTime != null) "Completed" else "In Office"
            binding.tvStatus.text = status
            binding.tvStatus.setBackgroundResource(
                if (record.checkOutTime != null)
                    com.autoattendance.R.drawable.bg_status_done
                else
                    com.autoattendance.R.drawable.bg_status_active
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<AttendanceRecord>() {
        override fun areItemsTheSame(oldItem: AttendanceRecord, newItem: AttendanceRecord) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AttendanceRecord, newItem: AttendanceRecord) =
            oldItem == newItem
    }
}
