package com.example.capstonandroid.dto

data class User(
    val birth: String,
    val created_at: String,
    val email: String,
    val followers: List<Follower>,
    val followings: List<Any>,
    val id: Int,
    val introduce: String,
    val location: String,
    val mmr: Int,
    val name: String,
    val posts: List<Post>,
    val profile: String,
    val sex: String,
    val updated_at: String,
    val weight: String
)