package com.example.capstonandroid

data class Record(
    val altitude: Int,
    val altitudes: ArrayList<Int>,
    val averageSpeed: Double,
    val calorie: Double,
    val coordinates: ArrayList<List<Double>>,
    val distance: Double,
    val kind: String,
    val range: String,
    val speeds: ArrayList<Double>,
    val time: Int,
    val times: ArrayList<Long>
)