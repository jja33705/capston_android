package com.example.capstonandroid.network.dto

data class Badges(
    val first_exercise: Boolean,
    val altitude: Boolean,
    val altitude2: Boolean,
    val altitude3: Boolean,
    val bike_distance: Boolean,
    val bike_distance2: Boolean,
    val bike_distance3: Boolean,
    val make_track: Boolean,
    val make_track2: Boolean,
    val make_track3: Boolean,
    val run_distance: Boolean,
    val run_distance2: Boolean,
    val run_distance3: Boolean,
    val updated_at: String,
    val created_at: String,
    val id: Int,
    val user_id: Int
)