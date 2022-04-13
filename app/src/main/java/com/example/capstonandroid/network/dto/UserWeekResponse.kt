package com.example.capstonandroid.network.dto

data class UserWeekResponse(
    val fiveDayAgo: Double,
    val fourDayAgo: Double,
    val oneDayAgo: Double,
    val sixDayAgo: Double,
    val threeDayAgo: Double,
    val today: Double,
    val twoDayAgo: Double
)