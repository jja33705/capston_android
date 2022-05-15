package com.example.capstonandroid.network.dto

data class Profile(
    val badges: Any,
    val bikePercentage: Int,
    val bikeWeekData: UserWeekResponse,
    val birth: String,
    val created_at: String,
    val email: String,
    val fcm_token: Any,
    val followCheck: Int,
    val followers: List<User>,
    val followings: List<User>,
    val id: Int,
    val introduce: String,
    val location: String,
    val mmr: Int,
    val name: String,
    val profile: String,
    val runPercentage: Int,
    val runWeekData: UserWeekResponse,
    val run_mmr: Int,
    val sex: String,
    val updated_at: String,
    val weight: Int
)