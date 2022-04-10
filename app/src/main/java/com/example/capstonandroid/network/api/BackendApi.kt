package com.example.capstonandroid.network.api

// 사용할 api 목록 정의하는 곳
// API 선언 interface

import com.example.capstonandroid.network.dto.*
import retrofit2.Call
import retrofit2.http.*
import com.example.capstonandroid.network.dto.GetTracksResponse
import com.example.capstonandroid.network.dto.Track
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

// 사용할 api 목록 정의하는 곳
interface BackendApi {

//   대영 코드
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
    fun SNSIndex(@Header("Authorization") token: String,@Query("page") page: Int): Call<SNSResponse>

    @GET("post/myIndex") // 내 기록 불러오기!
    fun myIndex(@Header("Authorization")token: String,@Query("page") page: Int): Call<MySNSResponse>

    @POST("userSearch") // 유저 검색ㅋㅋ
    fun userSearch(@Body userSearchResponse: UserSearchResponse) : Call<UserSearchResponse>

    @PUT("post/update/{postID}") // 자기 게시물 수정
    fun postUpdate(@Header("Authorization") token : String, @Path("postID")postID : Int, @Body update : Update) : Call<Int>

    @DELETE("post/{postID}") // 자기 게시물 삭제
    fun postDelete(@Header("Authorization") token: String,@Path("postID") postID :Int ) : Call<DeleteResponse>

    @GET("post/weekRecord") //요일별 누적거리
    fun userWeek(@Header("Authorization") token : String,@Query("event") event: String) : Call<UserWeekResponse>

    @POST("like/{postID}") //좋아요 누르기
    fun postLike(@Header("Authorization") token:String, @Path("postID") postID: Int) : Call<LikeResponse>

    @POST("comment/store/{postID}") // 댓글 등록..
    fun commentSend(@Header("Authorization")token: String, @Path("postID") postID:Int,@Body commentSend: CommentSend) : Call<CommentSendResponse>

    @DELETE("post/{postID}")


    @POST("post/image")
    fun imageTest(@Header("Authorization") token : String ,
                  @Part images : MultipartBody.Part?): Call<ImageResponse>




//  재현 코드


    @GET("test") // 보낼 url
    fun test(): Call<String>

//  @POST("gps")
//  fun uploadGpsData(@Body positions: Positions) : Call<Positions>

//  @POST("post/store") // 기록 저장
//  fun storePost(@Body record: Track) : Call<Track>

    @POST("post/store") // 포스트 작성
    suspend fun postRecordActivity(@Header("Authorization") token: String, @Body postRecordActivity: PostRecordActivity): Response<ResponseMessage>

    @GET("match/rank") // 랭크 랜덤 매칭
    suspend fun rankMatching(@Header("Authorization") token: String, @Query("track_id") trackId: String): Response<RankMatchingResponse>

    @GET("match/gpsData") // gps 데이터 받기
    suspend fun getGpsData(@Header("Authorization") token: String, @Query("gpsId") gpsId: String): Response<GetGpsDataResponse>

    @GET("tracks/search") // 트랙 리스트 받기
    suspend fun getTracks(@Header("Authorization") token: String, @Query("bound1") bound1: Double, @Query("bound2") bound2: Double, @Query("bound3") bound3: Double, @Query("bound4") bound4: Double, @Query("zoom") zoom: Int, @Query("event") event: String): Response<GetTracksResponse>

    @GET("tracks") // 한개 트랙 받기
    suspend fun getTrack(@Header("Authorization") token: String, @Query("track_id") id: String): Response<Track>

    @GET("ranking/track") // 트랙 랭킹 받기
    suspend fun getRanking(@Header("Authorization") token: String, @Query("track_id") id: String, @Query("page") page: Int): Response<RankingResponse>
}
