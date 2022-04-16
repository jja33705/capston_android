package com.example.capstonandroid

data class FriendlyMatchingItem(
    val postTitle: String,
    val image: String?,
    val userName: String,
    val date: String,
    val time: Int,
    val speed: Double,
    val opponentGpsDataId: String,
    val opponentPostId: Int,
)
