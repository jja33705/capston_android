package com.example.capstonandroid.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.capstonandroid.db.dao.GpsDataDao
import com.example.capstonandroid.db.dao.OpponentGpsDataDao
import com.example.capstonandroid.db.entity.GpsData
import com.example.capstonandroid.db.entity.OpponentGpsData

@Database(entities = [GpsData::class, OpponentGpsData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gpsDataDao(): GpsDataDao
    abstract fun opponentGpsDataDao(): OpponentGpsDataDao

    companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase? {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app_database").build()
                }
            }
            return instance
        }
    }
}