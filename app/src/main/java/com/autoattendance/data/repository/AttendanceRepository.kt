package com.autoattendance.data.repository

import android.content.Context
import com.autoattendance.data.db.AppDatabase
import com.autoattendance.model.AttendanceRecord
import com.autoattendance.model.Employee
import com.autoattendance.model.OfficeLocation
import java.text.SimpleDateFormat
import java.util.*

class AttendanceRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val employeeDao = db.employeeDao()
    private val attendanceDao = db.attendanceDao()
    private val locationDao = db.officeLocationDao()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // --- Employees ---
    val allEmployees = employeeDao.getAllEmployees()

    suspend fun addEmployee(employee: Employee) = employeeDao.insert(employee)
    suspend fun updateEmployee(employee: Employee) = employeeDao.update(employee)
    suspend fun deleteEmployee(employee: Employee) = employeeDao.delete(employee)
    suspend fun getEmployeeByEmpId(empId: String) = employeeDao.getByEmployeeId(empId)
    suspend fun getAllEmployeesSync() = employeeDao.getAllEmployeesSync()

    // --- Attendance ---
    val allRecords = attendanceDao.getAllRecords()

    fun getRecordsByDate(date: String) = attendanceDao.getRecordsByDate(date)
    fun getRecordsByEmployee(employeeId: Int) = attendanceDao.getRecordsByEmployee(employeeId)

    suspend fun checkIn(employee: Employee, location: OfficeLocation) {
        val today = dateFormat.format(Date())
        val existing = attendanceDao.getTodayRecord(employee.id, today)
        if (existing != null) return  // already checked in today

        val record = AttendanceRecord(
            employeeDbId = employee.id,
            employeeId = employee.employeeId,
            employeeName = employee.name,
            locationName = location.name,
            latitude = location.latitude,
            longitude = location.longitude,
            checkInTime = System.currentTimeMillis(),
            date = today
        )
        attendanceDao.insert(record)
    }

    suspend fun checkOut(employee: Employee) {
        val openRecord = attendanceDao.getOpenRecord(employee.id) ?: return
        attendanceDao.update(openRecord.copy(checkOutTime = System.currentTimeMillis()))
    }

    suspend fun getTodayCount(): Int {
        val today = dateFormat.format(Date())
        return attendanceDao.getCountByDate(today)
    }

    // --- Office Locations ---
    val allLocations = locationDao.getAllLocations()

    suspend fun addLocation(location: OfficeLocation) = locationDao.insert(location)
    suspend fun updateLocation(location: OfficeLocation) = locationDao.update(location)
    suspend fun deleteLocation(location: OfficeLocation) = locationDao.delete(location)
    suspend fun getActiveLocations() = locationDao.getActiveLocations()
    suspend fun getLocationById(id: Int) = locationDao.getById(id)
}
