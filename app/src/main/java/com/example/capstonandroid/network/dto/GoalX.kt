package com.example.capstonandroid.network.dto

data class GoalX(
    val created_at: String,
    val event: String,
    val firstDate: String,
    val goalDistance: Int,
    val id: Int,
    val lastDate: String,
    val success: Boolean,
    val title: String,
    val updated_at: String,
    val user_id: Int
)