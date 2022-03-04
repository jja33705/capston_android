package com.example.capstonandroid.dto

data class TracksResponse(
    val message: String,
    val result: ArrayList<Track>,
    val zoom: String
)