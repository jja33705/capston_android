package com.example.capstonandroid.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.capstonandroid.db.entity.GpsData

@Dao
interface GpsDataDao {
    @Query("SELECT * FROM gps_data")
    fun getAllGpsData(): List<GpsData>

    @Insert
    fun insertGpsData(gpsData: GpsData)

    @Query("DELETE FROM gps_data")
    fun deleteAllGpsData()
}