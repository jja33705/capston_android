package com.example.capstonandroid.network.dto

data class Data(
    val altitude: Double,
    val average_speed: Double,
    val calorie: Double,
    val content: String,
    val created_at: String,
    val distance: Double,
    val event: String,
    val gps_id: Int,
    val id: Int,
    val img: Any,
    val likes: List<Any>,
    val mmr: Int,
    val range: String,
    val time: Int,
    val title: String,
    val track_id: Int,
    val updated_at: String,
    val user: User,
    val user_id: Int
)