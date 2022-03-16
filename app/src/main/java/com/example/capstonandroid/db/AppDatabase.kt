package com.example.capstonandroid.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.capstonandroid.db.dao.GpsDataDao
import com.example.capstonandroid.db.entity.GpsData

@Database(entities = [GpsData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gpsDataDao(): GpsDataDao
}