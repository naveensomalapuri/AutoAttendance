package com.autoattendance.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.autoattendance.model.AttendanceRecord

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AttendanceRecord): Long

    @Update
    suspend fun update(record: AttendanceRecord)

    @Query("SELECT * FROM attendance_records ORDER BY checkInTime DESC")
    fun getAllRecords(): LiveData<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date ORDER BY checkInTime DESC")
    fun getRecordsByDate(date: String): LiveData<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE employeeDbId = :employeeId ORDER BY checkInTime DESC")
    fun getRecordsByEmployee(employeeId: Int): LiveData<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE employeeDbId = :employeeId AND date = :date LIMIT 1")
    suspend fun getTodayRecord(employeeId: Int, date: String): AttendanceRecord?

    @Query("SELECT * FROM attendance_records WHERE checkOutTime IS NULL AND employeeDbId = :employeeId")
    suspend fun getOpenRecord(employeeId: Int): AttendanceRecord?

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date = :date")
    suspend fun getCountByDate(date: String): Int

    @Query("DELETE FROM attendance_records WHERE date < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: String)
}
