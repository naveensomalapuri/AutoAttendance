package com.autoattendance.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.autoattendance.model.AttendanceRecord
import com.autoattendance.model.Employee
import com.autoattendance.model.OfficeLocation

@Database(
    entities = [Employee::class, AttendanceRecord::class, OfficeLocation::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun employeeDao(): EmployeeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun officeLocationDao(): OfficeLocationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "auto_attendance.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
