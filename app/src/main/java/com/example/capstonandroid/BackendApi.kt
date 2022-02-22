package com.example.capstonandroid

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

// 사용할 api 목록 정의하는 곳
interface BackendApi {
//    @POST("gps")
//    fun uploadGpsData(@Body positions: Positions) : Call<Positions>

    @FormUrlEncoded
    @POST("test") // 보낼 url
    fun test(@Field("test") test: String) : Call<Test>

    @POST("post/store") // 기록 저장
    fun storePost(@Body record: Record) : Call<Record>
}
