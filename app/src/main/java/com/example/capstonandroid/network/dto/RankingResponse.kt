package com.example.capstonandroid.network.dto

data class RankingResponse(
    val current_page: Int,
    val `data`: List<Data>,
    val first_page_url: String,
    val from: Int,
    val last_page: Int,
    val last_page_url: String,
    val links: List<Link>,
    val next_page_url: String?,
    val path: String,
    val per_page: Int,
    val prev_page_url: String?,
    val to: Int,
    val total: Int
)