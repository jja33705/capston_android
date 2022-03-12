package com.example.capstonandroid.network.dto

data class GetTracksResponse(
    val message: String,
    val result: ArrayList<Track>,
    val zoom: String
)