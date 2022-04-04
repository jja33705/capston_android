package com.example.capstonandroid.network.dto

import android.graphics.drawable.Drawable

data class UserData(
    val img : Drawable,
    val title : String,
    val name: String,
    val data_num : Int,
    val created_id : String,
    val time : Int,
    val page : Int,
    )