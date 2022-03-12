package com.example.capstonandroid.dto

data class RegisterResponse(
    val name: String,
    val email: String,
    val sex: String,
    val weight: String,
    val profile: String,
    val birth: String,
    val introduce: String,
    val location: String,
    val mmr: Int,
    val updated_at: String,
    val created_at: String,
    val id: Int,
)