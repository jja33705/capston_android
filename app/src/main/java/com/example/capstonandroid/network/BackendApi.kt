package com.example.capstonandroid.network

import com.example.capstonandroid.dto.*
import retrofit2.Call
import retrofit2.http.*

// 사용할 api 목록 정의하는 곳
// API 선언 interface
interface BackendApi {
//    @POST("gps")
//    fun uploadGpsData(@Body positions: Positions) : Call<Positions>


    @FormUrlEncoded
    @POST("test") // 보낼 url
    fun test(@Field("test") test: String) : Call<Test>

    @POST("post/store") // 기록 저장
    fun storePost(@Body record: Record) : Call<Record>

    @POST("login") //로그인 요청(Login) 하고 응답 받는것(LoginResponse)
    fun loginPost(@Body login: Login) : Call<LoginResponse>

    @POST("register") //회원가입 요청(Register) 하고 응답받는것
    fun registerPost(@Body register: Register) : Call<RegisterResponse>


    @GET("user") // 유저확인 ()
    fun userGet(@Header("Authorization") token: String) : Call<Int>

    @POST("logout") // 유저 로그아웃
    fun  logOut(@Header("Authorization") token: String) : Call<LogoutResponse>
}
