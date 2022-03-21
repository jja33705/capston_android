package com.example.capstonandroid.network.dto

import android.graphics.drawable.Drawable

data class UserData(
    var img : Drawable,
    var id: String,
    val name: String
)