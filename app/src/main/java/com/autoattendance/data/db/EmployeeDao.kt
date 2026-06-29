package com.autoattendance.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.autoattendance.model.Employee

@Dao
interface EmployeeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(employee: Employee): Long

    @Update
    suspend fun update(employee: Employee)

    @Delete
    suspend fun delete(employee: Employee)

    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): LiveData<List<Employee>>

    @Query("SELECT * FROM employees ORDER BY name ASC")
    suspend fun getAllEmployeesSync(): List<Employee>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getById(id: Int): Employee?

    @Query("SELECT * FROM employees WHERE employeeId = :empId")
    suspend fun getByEmployeeId(empId: String): Employee?

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun getCount(): Int
}
