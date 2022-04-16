package com.example.capstonandroid.network.dto

data class Post(
    val altitude: Double,
    val average_speed: Double,
    val calorie: Double,
    val comment: List<Comment>,
    val content: String,
    val created_at: String,
    val date: String,
    val distance: Double,
    val event: String,
    val gps_id: String,
    val id: Int,
    val image: List<Image>,
    val img: String,
    val kind: String,
    val likes: List<Like>,
    val map_image: List<MapImage>,
    val mmr: Int,
    val opponent_id: Int,
    val range: String,
    val time: Int,
    val title: String,
    val track_id: Any,
    val updated_at: String,
    val user: User,
    val user_id: Int
)