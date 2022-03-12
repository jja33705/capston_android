package com.example.capstonandroid.network.dto

data class Gps(
    val _id: String,
    val coordinates: List<List<Double>>,
    val type: String
)