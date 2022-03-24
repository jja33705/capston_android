package com.example.capstonandroid.network.dto

data class GpsData(
    val __v: Int,
    val _id: String,
    val altitude: List<Double>,
    val createdAt: String,
    val distance: List<Double>,
    val event: String,
    val gps: Gps,
    val speed: List<Double>,
    val time: List<Int>,
    val totalTime: Int,
    val trackId: String,
    val user: UserIdAndName
)