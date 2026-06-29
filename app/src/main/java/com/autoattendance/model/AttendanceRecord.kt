package com.autoattendance.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeDbId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("employeeDbId")]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val employeeDbId: Int,
    val employeeId: String,       // denormalized for quick display
    val employeeName: String,     // denormalized for quick display
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val checkInTime: Long?,
    val checkOutTime: Long? = null,
    val date: String              // "YYYY-MM-DD" for easy filtering
)
