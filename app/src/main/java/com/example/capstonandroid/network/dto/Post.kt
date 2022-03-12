package com.example.capstonandroid.dto

import android.graphics.drawable.Drawable

data class Post(
    val altitude: Int,
    val average_speed: Int,
    val calorie: Double,
    val content: String,
    val created_at: String,
    val distance: Double,
    val event: String,
    val gps_id: Int,
    val id: Int,
    val img: Any,
    val mmr: Int,
    val range: String,
    val time: Int,
    val title: String,
    val track_id: Int,
    val updated_at: String,
    val user_id: Int
)