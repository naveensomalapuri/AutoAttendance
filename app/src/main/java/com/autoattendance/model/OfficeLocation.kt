package com.autoattendance.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "office_locations")
data class OfficeLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float = 100f,   // geofence radius
    val isActive: Boolean = true
)
