package com.example.capstonandroid

data class Baemin(
    val data: Data
    )

data class Data(
    val content: ArrayList<Content>
    )

data class Content(
    val title: String,
    val created: String
    )