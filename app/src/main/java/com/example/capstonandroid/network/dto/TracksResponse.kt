package com.example.capstonandroid.network.dto

data class TracksResponse(
    val message: String,
    val result: ArrayList<Track>,
    val zoom: String
)