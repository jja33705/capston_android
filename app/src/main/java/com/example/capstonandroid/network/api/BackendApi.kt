package com.example.capstonandroid.network.api

// 사용할 api 목록 정의하는 곳
// API 선언 interface

import com.example.capstonandroid.network.dto.*
import retrofit2.Call
import retrofit2.http.*
import com.example.capstonandroid.network.dto.GetTracksResponse
import com.example.capstonandroid.network.dto.Track
import retrofit2.Response

// 사용할 api 목록 정의하는 곳
interface BackendApi {
//    @POST("gps")
//    fun uploadGpsData(@Body positions: Positions) : Call<Positions>

//   테스트용..



    @GET("test") // 보낼 url
    fun test(): Call<String>

    @POST("login") //로그인 요청(Login) 하고 응답 받는것(LoginResponse)
    fun loginPost(@Body login: Login): Call<LoginResponse>

    @POST("register") //회원가입 요청(Register) 하고 응답받는것
    fun registerPost(@Body register: Register): Call<RegisterResponse>

    @GET("user") // 유저확인 ()
    fun userGet(@Header("Authorization") token: String): Call<LoginUserResponse>

    @POST("logout") // 유저 로그아웃
    fun logOut(@Header("Authorization") token: String): Call<LogoutResponse>
//
    @GET("post/index") // SNS 메인화면~
    fun SNSIndex(@Header("Authorization") token: String): Call<SNSResponse>

    @GET("record/myIndex") // 내 기록 불러오기!
    fun myIndex(@Header("Authorization")token: String): Call<IndexResponse>

    //    @POST("post/store") // 기록 저장
    //    fun storePost(@Body record: Track) : Call<Track>

    @POST("post/store") // 포스트 작성
    suspend fun postRecordActivity(@Header("Authorization") token: String, @Body postRecordActivity: PostRecordActivity): Response<ResponseMessage>

    @POST("match/rank") // 랭크 랜덤 매칭
    suspend fun rankMatching(@Header("Authorization") token: String, @Body trackId: TrackId): Response<RankMatchingResponse>

    @POST("match/gpsData") // gps 데이터 받기
    suspend fun getGpsData(@Header("Authorization") token: String, @Body gpsId: GpsDataId): Response<GetGpsDataResponse>

    @GET("/api/tracks/search") // 트랙 리스트 받기
    suspend fun getTracks(
        @Header("Authorization") token: String,
        @Query("bound1") bound1: Double,
        @Query("bound2") bound2: Double,
        @Query("bound3") bound3: Double,
        @Query("bound4") bound4: Double,
        @Query("zoom") zoom: Int,
        @Query("event") event: String
    ): Response<GetTracksResponse>

    @GET("/api/tracks/{id}") // 한개 트랙 받기
    suspend fun getTrack(@Header("Authorization") token: String, @Path("id") id: String): Response<Track>
}
