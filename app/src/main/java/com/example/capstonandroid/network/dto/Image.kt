package com.example.capstonandroid.network.dto

data class Image(
    val created_at: String,
    val id: Int,
    val image: String,
    val post_id: Int,
    val updated_at: String,
    val url: String
)