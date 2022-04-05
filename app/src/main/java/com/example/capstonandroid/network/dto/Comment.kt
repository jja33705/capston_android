package com.example.capstonandroid.network.dto

data class Comment(
    val content: String,
    val created_at: String,
    val id: Int,
    val post_id: Int,
    val replies: List<Any>,
    val updated_at: String,
    val user: User,
    val user_id: Int
)