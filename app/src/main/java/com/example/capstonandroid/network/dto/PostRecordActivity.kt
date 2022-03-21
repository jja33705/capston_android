package com.example.capstonandroid.network.dto

data class PostRecordActivity(
    val altitude: Double,
    val average_speed: Double,
    val calorie: Double,
    val content: String,
    val distance: Double,
    val event: String,
    val kind: String,
    val range: String,
    val time: Int,
    val title: String,
    val track_id: Int?,
    val gpsData: PostRecordGpsData
)