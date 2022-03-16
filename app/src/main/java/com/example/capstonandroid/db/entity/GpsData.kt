package com.example.capstonandroid.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gps_data")
data class GpsData(
    @PrimaryKey val second: Int,
    val lat: Double,
    val lng: Double,
    val speed: Float,
    val distance: Double,
    val altitude: Double,
)
