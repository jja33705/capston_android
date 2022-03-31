package com.example.capstonandroid.network.dto

data class Ranking(
    val altitude: Int,
    val average_speed: Int,
    val calorie: Double,
    val content: String,
    val created_at: String,
    val date: String,
    val distance: Int,
    val event: String,
    val gps_id: String,
    val id: Int,
    val img: String,
    val kind: String,
    val mmr: Int,
    val opponent_id: Int,
    val range: String,
    val time: Int,
    val title: String,
    val track_id: String,
    val updated_at: String,
    val user: User,
    val user_id: Int
)