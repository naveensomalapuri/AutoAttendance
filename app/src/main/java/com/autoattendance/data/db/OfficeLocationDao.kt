package com.autoattendance.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.autoattendance.model.OfficeLocation

@Dao
interface OfficeLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: OfficeLocation): Long

    @Update
    suspend fun update(location: OfficeLocation)

    @Delete
    suspend fun delete(location: OfficeLocation)

    @Query("SELECT * FROM office_locations ORDER BY name ASC")
    fun getAllLocations(): LiveData<List<OfficeLocation>>

    @Query("SELECT * FROM office_locations WHERE isActive = 1")
    suspend fun getActiveLocations(): List<OfficeLocation>

    @Query("SELECT * FROM office_locations WHERE id = :id")
    suspend fun getById(id: Int): OfficeLocation?
}
