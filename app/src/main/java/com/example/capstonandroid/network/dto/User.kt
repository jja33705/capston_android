package com.example.capstonandroid.network.dto

data class User(
    val birth: String,
    val created_at: String,
    val email: String,
    val followers: List<User>,
    val followings: List<User>,
    val id: Int,
    val introduce: String,
    val location: String,
    val mmr: Int,
    val name: String,
    val posts: List<Post>,
    val profile: String,
    val sex: String,
    val updated_at: String,
    val weight: Int,
    val pivot: Pivot
)