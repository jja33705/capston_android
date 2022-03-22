package com.example.capstonandroid.network.dto

data class Post(
    val altitude: Double,
    val average_speed: Double,
    val calorie: Double,
    val content: String,
    val created_at: String,
    val date: String,
    val distance: Double,
    val event: String,
    val gps_id: String,
    val id: Int,
    val img: Any,
    val kind: String,
    val mmr: Int,
    val range: String,
    val time: Int,
    val title: String,
    val track_id: String,
    val updated_at: String,
    val user_id: Int,
)