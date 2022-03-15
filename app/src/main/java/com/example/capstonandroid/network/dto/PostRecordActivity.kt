package com.example.capstonandroid.network.dto

import com.example.capstonandroid.db.entity.GpsData

data class PostRecordActivity(
    val altitude: Int,
    val average_speed: Double,
    val calorie: Int,
    val content: String,
    val distance: Double,
    val event: String,
    val kind: String,
    val range: String,
    val time: Int,
    val title: String,
    val track_id: Int?,
    val gpsData: List<GpsData>
)