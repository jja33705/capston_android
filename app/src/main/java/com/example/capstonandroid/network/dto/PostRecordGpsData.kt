package com.example.capstonandroid.network.dto

data class PostRecordGpsData(
    val speed: ArrayList<Float>,
    val gps: ArrayList<List<Double>>,
    val altitude: ArrayList<Double>,
    val distance: ArrayList<Double>,
    val time: ArrayList<Int>,
)
