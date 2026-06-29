package com.autoattendance.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.autoattendance.data.repository.AttendanceRepository
import com.autoattendance.model.AttendanceRecord
import com.autoattendance.model.Employee
import com.autoattendance.model.OfficeLocation
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AttendanceRepository(application)

    val allEmployees = repository.allEmployees
    val allAttendanceRecords = repository.allRecords
    val allLocations = repository.allLocations

    private val _todayCount = MutableLiveData<Int>(0)
    val todayCount: LiveData<Int> = _todayCount

    private val _selectedDate = MutableLiveData(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedDate: LiveData<String> = _selectedDate

    val recordsByDate: LiveData<List<AttendanceRecord>> = _selectedDate.switchMap { date ->
        repository.getRecordsByDate(date)
    }

    init {
        refreshTodayCount()
    }

    // --- Employee operations ---
    fun addEmployee(employee: Employee) = viewModelScope.launch {
        repository.addEmployee(employee)
    }

    fun updateEmployee(employee: Employee) = viewModelScope.launch {
        repository.updateEmployee(employee)
    }

    fun deleteEmployee(employee: Employee) = viewModelScope.launch {
        repository.deleteEmployee(employee)
    }

    // --- Location operations ---
    fun addLocation(location: OfficeLocation) = viewModelScope.launch {
        repository.addLocation(location)
    }

    fun updateLocation(location: OfficeLocation) = viewModelScope.launch {
        repository.updateLocation(location)
    }

    fun deleteLocation(location: OfficeLocation) = viewModelScope.launch {
        repository.deleteLocation(location)
    }

    // --- Attendance ---
    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun refreshTodayCount() = viewModelScope.launch {
        _todayCount.value = repository.getTodayCount()
    }
}
