package com.example.capstonandroid.network.dto

data class Track(
    val __v: Int,
    val _id: String,
    val altitude: List<Int>,
    val checkPoint: List<List<Int>>,
    val createdAt: String,
    val description: String,
    val distance: Int,
    val end_latlng: List<Double>,
    val event: String,
    val gps: Gps,
    val name: String,
    val start_latlng: List<Double>,
    val userId: Int
)