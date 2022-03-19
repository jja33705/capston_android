package com.example.capstonandroid.network.dto

data class LoginResponse(
    val access_token: String,
    val user: User,
    val test: String
)