package com.example.capstonandroid.network.dto

data class Track(
    val __v: Int,
    val _id: String,
    val altitude: List<Double>,
    val checkPoint: List<List<Double>>,
    val createdAt: String,
    val description: String,
    val event: String,
    val gps: Gps,
    val totalDistance: Double,
    val trackName: String,
    val avgSlope: Double,
    val user: UserIdAndName
)