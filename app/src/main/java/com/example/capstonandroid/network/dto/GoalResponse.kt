package com.example.capstonandroid.network.dto

data class GoalResponse(
    val event: String,
    val firstDate: String,
    val goal: Int,
    val lastDate: String,
    val title: String,
    val created_at: String,
    val goalDistance: Int,
    val id: Int,
    val success: Boolean,
    val updated_at: String,
    val user_id: Int
)