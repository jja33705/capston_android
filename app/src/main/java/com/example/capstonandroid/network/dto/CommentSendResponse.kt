package com.example.capstonandroid.network.dto

data class CommentSendResponse(
    val comment: Comment,
    val message: List<String>
)