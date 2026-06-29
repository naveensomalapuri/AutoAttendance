package com.autoattendance.ui.employee

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.autoattendance.databinding.ItemEmployeeBinding
import com.autoattendance.model.Employee

class EmployeeAdapter(
    private val onDelete: (Employee) -> Unit
) : ListAdapter<Employee, EmployeeAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemEmployeeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(employee: Employee) {
            binding.tvName.text = employee.name
            binding.tvEmployeeId.text = employee.employeeId
            binding.tvDepartment.text = employee.department.ifEmpty { "—" }
            binding.tvInitial.text = employee.name.first().uppercase()
            binding.btnDelete.setOnClickListener { onDelete(employee) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEmployeeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Employee>() {
        override fun areItemsTheSame(oldItem: Employee, newItem: Employee) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Employee, newItem: Employee) = oldItem == newItem
    }
}
