package com.example.capstonandroid.network.dto

data class Bike(
    val created_at: String,
    val event: String,
    val firstDate: String,
    val goalDistance: Int,
    val id: Int,
    val lastDate: String,
    val progress: Int,
    val success: Int,
    val title: String,
    val updated_at: String,
    val user_id: Int
)