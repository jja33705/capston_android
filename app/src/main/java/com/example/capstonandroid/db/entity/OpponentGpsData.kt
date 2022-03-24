package com.example.capstonandroid.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "opponent_gps_data")
data class OpponentGpsData(
    @PrimaryKey val second: Int,
    val lat: Double,
    val lng: Double,
    val speed: Float,
    val distance: Double,
    val altitude: Double,
)
