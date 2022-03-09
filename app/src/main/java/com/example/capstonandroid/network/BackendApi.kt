package com.example.capstonandroid.network

import com.example.capstonandroid.dto.GetTracksResponse
import com.example.capstonandroid.dto.Test
import com.example.capstonandroid.dto.Track
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

// 사용할 api 목록 정의하는 곳
interface BackendApi {
//    @POST("gps")
//    fun uploadGpsData(@Body positions: Positions) : Call<Positions>

    @FormUrlEncoded
    @POST("test") // 보낼 url
    fun test(@Field("test") test: String) : Call<Test>

//    @POST("post/store") // 기록 저장
//    fun storePost(@Body record: Track) : Call<Track>

    @GET // 트랙 리스트 받기
    suspend fun getTracks(@Url url: String, @Query("bounds") bounds1: List<Double>, @Query("zoom") zoom: Int, @Query("event") event: String): Response<GetTracksResponse>

    @GET // 한개 트랙 받기
    suspend fun getTrack(@Url url: String): Response<Track>
}
