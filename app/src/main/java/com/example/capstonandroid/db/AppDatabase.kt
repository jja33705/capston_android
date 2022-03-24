package com.example.capstonandroid.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.capstonandroid.db.dao.GpsDataDao
import com.example.capstonandroid.db.dao.OpponentGpsDataDao
import com.example.capstonandroid.db.entity.GpsData
import com.example.capstonandroid.db.entity.OpponentGpsData

@Database(entities = [GpsData::class, OpponentGpsData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gpsDataDao(): GpsDataDao
    abstract fun opponentGpsDataDao(): OpponentGpsDataDao
}