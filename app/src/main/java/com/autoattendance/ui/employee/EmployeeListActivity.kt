package com.autoattendance.ui.employee

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.autoattendance.databinding.ActivityEmployeeListBinding
import com.autoattendance.model.Employee
import com.autoattendance.viewmodel.AttendanceViewModel

class EmployeeListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmployeeListBinding
    private val viewModel: AttendanceViewModel by viewModels()
    private lateinit var adapter: EmployeeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = "Employees"
            setDisplayHomeAsUpEnabled(true)
        }

        setupRecyclerView()

        binding.fabAddEmployee.setOnClickListener {
            showAddEmployeeDialog()
        }

        viewModel.allEmployees.observe(this) { employees ->
            adapter.submitList(employees)
            binding.tvEmpty.visibility =
                if (employees.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = EmployeeAdapter(
            onDelete = { employee ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Employee")
                    .setMessage("Delete ${employee.name}? Their attendance records will also be removed.")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteEmployee(employee) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvEmployees.layoutManager = LinearLayoutManager(this)
        binding.rvEmployees.adapter = adapter
    }

    private fun showAddEmployeeDialog() {
        val dialogBinding = com.autoattendance.databinding.DialogAddEmployeeBinding.inflate(layoutInflater)

        AlertDialog.Builder(this)
            .setTitle("Add Employee")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val name = dialogBinding.etName.text.toString().trim()
                val empId = dialogBinding.etEmployeeId.text.toString().trim()
                val dept = dialogBinding.etDepartment.text.toString().trim()
                val phone = dialogBinding.etPhone.text.toString().trim()

                if (name.isNotEmpty() && empId.isNotEmpty()) {
                    viewModel.addEmployee(
                        Employee(
                            name = name,
                            employeeId = empId,
                            department = dept,
                            phone = phone
                        )
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
