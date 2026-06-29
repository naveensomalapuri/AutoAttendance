package com.autoattendance.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val employeeId: String,       // e.g. "EMP-001"
    val name: String,
    val department: String,
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
