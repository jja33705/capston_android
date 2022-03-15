package com.example.capstonandroid.network.api

// 사용할 api 목록 정의하는 곳
// API 선언 interface

import com.example.capstonandroid.network.dto.*
import retrofit2.Call
import retrofit2.http.*
import com.example.capstonandroid.network.dto.GetTracksResponse
import com.example.capstonandroid.network.dto.Test
import com.example.capstonandroid.network.dto.Track
import retrofit2.Response

// 사용할 api 목록 정의하는 곳
interface BackendApi {
//    @POST("gps")
//    fun uploadGpsData(@Body positions: Positions) : Call<Positions>


    @FormUrlEncoded
    @POST("test") // 보낼 url
    fun test(@Field("test") test: String): Call<Test>

    @POST("login") //로그인 요청(Login) 하고 응답 받는것(LoginResponse)
    fun loginPost(@Body login: Login): Call<LoginResponse>

    @POST("register") //회원가입 요청(Register) 하고 응답받는것
    fun registerPost(@Body register: Register): Call<RegisterResponse>


    @GET("user") // 유저확인 ()
    fun userGet(@Header("Authorization") token: String): Call<Int>

    @POST("logout") // 유저 로그아웃
    fun logOut(@Header("Authorization") token: String): Call<LogoutResponse>

    //    @POST("post/store") // 기록 저장
    //    fun storePost(@Body record: Track) : Call<Track>

    @POST("post/store") // 포스트 작성
    suspend fun postRecordActivity(@Header("Authorization") token: String, @Body postRecordActivity: PostRecordActivity): Response<ResponseMessage>

    @GET // 트랙 리스트 받기
    suspend fun getTracks(
        @Url url: String,
        @Query("bounds") bounds1: List<Double>,
        @Query("zoom") zoom: Int,
        @Query("event") event: String
    ): Response<GetTracksResponse>

    @GET // 한개 트랙 받기
    suspend fun getTrack(@Url url: String): Response<Track>
}
