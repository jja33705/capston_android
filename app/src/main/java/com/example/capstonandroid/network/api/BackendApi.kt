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
import retrofit2.Callback
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

    @PUT("post/update/{postId}") // 자기 게시물 수정
    suspend fun postUpdate(@Header("Authorization") token : String, @Path("postId")postId : Int, @Body update : Update) : Response<Int>

    @DELETE("post/{postId}") // 자기 게시물 삭제
    suspend fun postDelete(@Header("Authorization") token: String,@Path("postId") postId :Int ) : Response<Int>

    @GET("post/weekRecord") //요일별 누적거리
    fun userWeek(@Header("Authorization") token : String,@Query("event") event: String) : Call<UserWeekResponse>

//    @POST("like/{postID}") //좋아요 누르기
//    fun postLike(@Header("Authorization") token:String, @Path("postID") postID: Int) : Call<LikeResponse>

    @POST("comment/store/{postID}") // 댓글 등록..
    suspend fun commentSend(@Header("Authorization")token: String, @Path("postID") postID:Int,@Body commentSend: CommentSend) : Response<CommentSendResponse>

    @POST("follow/{userId}") // 팔로우하기~!
    suspend fun follow(@Header("Authorization") token : String, @Path("userId") userId : Int) : Response<FollowResponse>

    @POST("followRequest/{userId}")
    suspend fun followRequest(@Header("Authorization")token : String,@Path("userId") userId : Int): Response<Any>

    @POST("post/image") // 이미지 테스트
    fun imageTest(@Header("Authorization") token : String ,
                  @Part images : MultipartBody.Part?): Call<Image>

    @GET("record/type") //운동 비율
    fun userExerciseRate(@Header("Authorization") token :String): Call<UserExerciseRateResponse>

    @GET("goal/check")  //목표 체크
    fun userGoalCheck(@Header("Authorization") token : String) : Call<UserGoalCheckResponse>

    @POST("goal") // 목표 등록..
    fun goal(@Header("Authorization")token: String,@Body goal: Goal) : Call<GoalResponse>

    @DELETE("goal/delete/{goalID}") // 자기 게시물 삭제
    fun goalDelete(@Header("Authorization") token: String,@Path("goalID") goalID :Int ) : Call<goalDeleteResponse>

    @GET("userSearch") //유저 검색!!!!!!!!!
    fun userSearch(@Header("Authorization")token: String,@Query("keyword") keyword: String) : Call<SearchUserResponse>

    @GET("comment/index/{postId}") //댓글 검색!!!!!!!!!
    suspend fun commentIndex(@Header("Authorization")token: String,@Path("postId") postId: Int, @Query("page") page: Int) : Response<CommentIndexResponse>

    @POST("like/{postID}") //좋아요 누르기
    suspend fun postLike(@Header("Authorization") token:String, @Path("postID") postID: Int) : Response<LikeResponse>

    @GET("profile") //프로필 엿보기
    suspend fun getProfile(@Header("Authorization")token: String,@Query("me") me: Int,@Query("id") id : Int) : Response<Profile>

    @GET("record/distance") //거리
    suspend fun totalDistance(@Header("Authorization")token : String,@Query("event") event: String) : Response<DistanceResponse>

    @GET("record/totalTime") //　총시간
    suspend fun totalTime(@Header("Authorization")token : String) : Response<Int>

    @GET("record/totalCalorie") //총 칼로리
    suspend fun totalCalorie(@Header("Authorization")token : String) : Response<Double>


    @GET("record/altitude") // 총 고도
    suspend fun totalAltitude(@Header("Authorization")token : String) : Response<Double>

    @GET("record/trackCount") // 총 작성한 트랙 갯수
    fun  totalTrackCoutn(@Header("Authorization") token : String) : Call<Int>

    @GET("badge")
    fun getBadges(@Header("Authorization") token : String) : Call<Int>


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
    suspend fun getNotifications(@Header("Authorization") token: String): Response<GetNotificationsResponse>

    @DELETE("notification/delete/{notificationId}") // 알림 삭제
    suspend fun deleteNotification(@Header("Authorization") token: String, @Path("notificationId") notificationId: Int): Response<ResponseMessage>

    @PATCH("fcmToken") // fcmToken 저장
    suspend fun fcmToken(@Header("Authorization") token: String, @Body fcmToken: FcmToken): Response<ResponseMessage>

    @Multipart
    @POST("profile") // 프로필 수정
    suspend fun editProfile(@Header("Authorization") token: String, @Part profile: MultipartBody.Part, @Part("name") name: RequestBody, @Part("birth") birth: RequestBody, @Part("introduce") introduce: RequestBody, @Part("location") location: RequestBody, @Part("sex") sex: RequestBody, @Part("weight") weight: RequestBody): Response<User>

    @GET("userSearch") // 유저 검색
    suspend fun searchUser(@Header("Authorization") token: String, @Query("keyword") keyword: String, @Query("page") page: Int): Response<SearchUserResponse>

    @POST("unFollow/{userId}") // 팔로우 취소
    suspend fun unFollow(@Header("Authorization") token: String, @Path("userId") userId: Int): Response<ResponseMessage>

    @POST("cancel/{userId}") // 팔로우 요청 취소
    suspend fun cancelFollowRequest(@Header("Authorization") token: String, @Path("userId") userId: Int): Response<ResponseMessage>
}
