package com.example.capstonandroid.network.dto

data class GpsData(
    val user: User,
    val _id: String,
    val track_id: String,
    val gps: Gps,
    val speed: List<Double>,
    val time: List<Int>,
    val distance: List<Double>,
    val event: String,
    val altitude: List<Double>,
    val totalTime: Int,
    val createdAt: String,
    val __v: Int,
)
