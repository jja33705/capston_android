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
    suspend fun loginPost(@Body login: Login): Response<LoginResponse>

    @POST("register") //회원가입 요청(Register) 하고 응답받는것
    fun registerPost(@Body register: Register): Call<RegisterResponse>

    @GET("user") // 유저확인 ()
    fun userGet(@Header("Authorization") token: String): Call<LoginUserResponse>

    @POST("logout") // 유저 로그아웃
    fun logOut(@Header("Authorization") token: String): Call<LogoutResponse>

    @GET("post/index") // 메인화면 post list
    suspend fun getPosts(@Header("Authorization") token: String,@Query("page") page: Int): Response<GetPostsResponse>

    @GET("post/myIndex")
    suspend fun getMyPosts(@Header("Authorization") token: String,@Query("page") page: Int): Response<GetPostsResponse>

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

    @POST("follow/{userId}") // 팔로우하기~!
    suspend fun follow(@Header("Authorization") token : String, @Path("userId") userId : Int) : Response<FollowResponse>

    @POST("post/image") // 이미지 테스트
    fun imageTest(@Header("Authorization") token : String ,
                  @Part images : MultipartBody.Part?): Call<Image>

    @GET("record/type") //운동 비율
    fun userExerciseRate(@Header("Authorization") token :String): Call<UserExerciseRateResponse>

    @GET("goal/check")  //목표 체크
    fun userGoalCheck(@Header("Authorization") token : String) : Call<UserGoalCheckResponse>

    @POST("goal") // 댓글 등록..
    fun goal(@Header("Authorization")token: String,@Body goal: Goal) : Call<GoalResponse>

    @DELETE("goal/delete/{goalID}") // 자기 게시물 삭제
    fun goalDelete(@Header("Authorization") token: String,@Path("goalID") goalID :Int ) : Call<goalDeleteResponse>

    @GET("userSearch") //유저 검색!!!!!!!!!
    fun userSearch(@Header("Authorization")token: String,@Query("keyword") keyword: String) : Call<FollowerResponse>


//  재현 코드
//    @GET("test") // 보낼 url
//    fun test(): Call<String>

    @GET("post/show/{postId}")
    suspend fun getPost(@Header("Authorization") token: String, @Path("postId") postId: Int): Response<Post>

    @Multipart
    @POST("post/store") // 포스트 작성
    suspend fun postRecordActivity(
        @Header("Authorization") token: String,
        @Part mapImg: MultipartBody.Part,
        @Part img: List<MultipartBody.Part>?,
        @Part("average_speed") averageSpeed: RequestBody,
        @Part("altitude") altitude: RequestBody,
        @Part("calorie") calorie: RequestBody,
        @Part("content") content: RequestBody,
        @Part("distance") distance: RequestBody,
        @Part("event") event: RequestBody,
        @Part("kind") kind: RequestBody,
        @Part("range") range: RequestBody,
        @Part("time") time: RequestBody,
        @Part("title") title: RequestBody,
        @Part("track_id") trackId: RequestBody?,
        @Part("opponent_id") opponentId: RequestBody?,
        @Part("gpsData") gpsData: RequestBody
        ): Response<ResponseMessage>

    @GET("match/rank") // 랭크 랜덤 매칭
    suspend fun rankMatching(@Header("Authorization") token: String, @Query("track_id") trackId: String): Response<RankMatchingResponse>

    @GET("match/friendly") // 친선전 리스트
    suspend fun friendlyMatching(@Header("Authorization") token: String, @Query("track_id") trackId: String, @Query("page") page: Int): Response<FriendlyMatchingResponse>

    @GET("gpsData") // gps 데이터 받기
    suspend fun getGpsData(@Header("Authorization") token: String, @Query("gpsId") gpsId: String): Response<GetGpsDataResponse>

    @GET("tracks/search") // 트랙 리스트 받기
    suspend fun getTracks(@Header("Authorization") token: String, @Query("bound1") bound1: Double, @Query("bound2") bound2: Double, @Query("bound3") bound3: Double, @Query("bound4") bound4: Double, @Query("zoom") zoom: Int, @Query("event") event: String): Response<GetTracksResponse>

    @GET("tracks") // 한개 트랙 받기
    suspend fun getTrack(@Header("Authorization") token: String, @Query("track_id") id: String): Response<Track>

    @GET("ranking/track") // 트랙 랭킹 받기
    suspend fun getRanking(@Header("Authorization") token: String, @Query("track_id") id: String, @Query("page") page: Int): Response<RankingResponse>

    @GET("ranking/myRank") // 트랙에 내 순위
    suspend fun getMyRanking(@Header("Authorization") token: String, @Query("track_id") id: String): Response<MyRankingResponse>

    @GET("tracks/checkPoint") // 체크포인트에서 보내는 요청
    suspend fun checkpoint(@Header("Authorization") token: String, @Query("checkPoint") checkPointIndex: Int, @Query("track_id") trackId: String, @Query("time") time: Int): Response<CheckpointResponse>

    @GET("user") // 유저확인
    suspend fun getUser(@Header("Authorization") token: String): Response<User>

    @GET("notification") // 알림 리스트
    suspend fun getNotifications(@Header("Authorization") token: String, @Query("page") page: Int): Response<GetNotificationsResponse>

    @DELETE("notification/delete/{notificationId}") // 알림 삭제
    suspend fun deleteNotification(@Header("Authorization") token: String, @Path("notificationId") notificationId: Int): Response<ResponseMessage>

    @PATCH("fcmToken") // fcmToken 저장
    suspend fun fcmToken(@Header("Authorization") token: String, @Body fcmToken: FcmToken): Response<ResponseMessage>
}
