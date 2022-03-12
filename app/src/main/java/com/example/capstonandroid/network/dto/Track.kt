package com.example.capstonandroid.network.dto

data class Track(
    val __v: Int,
    val _id: String,
    val altitude: List<Int>,
    val checkPoint: List<List<Double>>,
    val createdAt: String,
    val description: String,
    val end_latlng: List<Double>,
    val event: String,
    val gps: Gps,
    val start_latlng: List<Double>,
    val totalDistance: Double,
    val trackName: String,
    val user: User
)