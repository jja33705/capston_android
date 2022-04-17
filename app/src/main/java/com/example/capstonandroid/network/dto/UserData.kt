package com.example.capstonandroid.network.dto

import android.graphics.drawable.Drawable

data class UserData(
    val img : Drawable,
    val title : String,
    val name: String,
    val data_num : Int,
    val created_id : String,
    val page : Int,
    val map_image: String,
    val profile : String,
    val likesize : Int
    )