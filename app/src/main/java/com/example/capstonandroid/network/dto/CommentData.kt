package com.example.capstonandroid.network.dto

data class CommentData(
    val username : String,
    val content: String,
    val created_at: String,
    val updated_at: String,
    val commentID : Int,
    val profile : String
)
