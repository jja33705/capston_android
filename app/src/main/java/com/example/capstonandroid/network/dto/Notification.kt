package com.example.capstonandroid.network.dto

data class Notification(
    val created_at: String,
    val mem_id: Int,
    val not_id: Int,
    val not_message: String,
    val not_type: String,
    val post_id: Int?,
    val read: Int,
    val target_mem_id: Int,
    val updated_at: String
)