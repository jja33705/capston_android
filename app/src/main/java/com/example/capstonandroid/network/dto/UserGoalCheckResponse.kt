package com.example.capstonandroid.network.dto

data class UserGoalCheckResponse(
    val bike: List<Bike>,
    val run: List<Run>
)