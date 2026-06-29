package com.autoattendance.ui.attendance

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.autoattendance.databinding.ActivityAttendanceHistoryBinding
import com.autoattendance.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.*

class AttendanceHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceHistoryBinding
    private val viewModel: AttendanceViewModel by viewModels()
    private lateinit var adapter: AttendanceAdapter

    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = "Attendance History"
            setDisplayHomeAsUpEnabled(true)
        }

        adapter = AttendanceAdapter()
        binding.rvAttendance.layoutManager = LinearLayoutManager(this)
        binding.rvAttendance.adapter = adapter

        binding.btnPickDate.setOnClickListener { showDatePicker() }

        viewModel.selectedDate.observe(this) { date ->
            val parsed = dbFormat.parse(date)
            binding.tvSelectedDate.text = if (parsed != null) displayFormat.format(parsed) else date
        }

        viewModel.recordsByDate.observe(this) { records ->
            adapter.submitList(records)
            binding.tvCount.text = "${records.size} record(s)"
            binding.tvEmpty.visibility =
                if (records.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selected = String.format("%04d-%02d-%02d", year, month + 1, day)
                viewModel.setSelectedDate(selected)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
