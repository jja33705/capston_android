package com.example.capstonandroid.network.dto

data class Like(
    val birth: String,
    val created_at: String,
    val email: String,
    val fcm_token: Any,
    val id: Int,
    val introduce: String,
    val location: String,
    val mmr: Int,
    val name: String,
    val pivot: Pivot,
    val profile: String,
    val sex: String,
    val updated_at: String,
    val weight: Int
)