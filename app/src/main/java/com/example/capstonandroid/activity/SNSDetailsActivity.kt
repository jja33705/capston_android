package com.example.capstonandroid.activity

// SNS 누르면 자세히 뜨는 것

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivitySnsdetailsBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.FollowResponse
import com.example.capstonandroid.network.dto.LikeResponse
import com.example.capstonandroid.network.dto.SNSResponse
import kotlinx.android.synthetic.main.item_view2.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class SNSDetailsActivity : AppCompatActivity() {
    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    private var page = 0       // 현재 페이지


    private var time = 0.0;
    private var calorie : Double = 0.0;
    private var average_speed : Double= 0.0;
    private var altitude: Double = 0.0;
    private var distance: Double = 0.0;
    private var kind = "";
    private var content = "";
    private var createdate = ""

    private var username = "";
    private var userID = 0

    lateinit var binding: ActivitySnsdetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snsdetails)


        binding = ActivitySnsdetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "활동"

        initRetrofit()

//        if (intent.hasExtra("indexnumber")) {
//            binding.textView10.text = intent.getStringExtra("indexnumber")
//            /* "nameKey"라는 이름의 key에 저장된 값이 있다면
//               textView의 내용을 "nameKey" key에서 꺼내온 값으로 바꾼다 */
//
//        } else {
//            Toast.makeText(this, "전달된 이름이 없습니다", Toast.LENGTH_SHORT).show()
//        }
//      누른 indexnumber~ 받아오기

        var postID = 0
        val data_num : Int = intent.getIntExtra("data_num",0)
        val data_page : Int = intent.getIntExtra("data_page",0)

        println(data_num.toString())
        println(data_page.toString())
        val sharedPreference = getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var token = "Bearer " + sharedPreference.getString("TOKEN","")
        println(token)


         supplementService.SNSIndex(token,data_page).enqueue(object : Callback<SNSResponse> {
             @RequiresApi(Build.VERSION_CODES.O)
             override fun onResponse(call: Call<SNSResponse>, response: Response<SNSResponse>) {

                 if(response.isSuccessful){

                     val defaultImage = R.drawable.map


                     if(response.body()!!.data[data_num].img ==null){
                         var url = ""
                         Glide.with(this@SNSDetailsActivity)
                             .load(url) // 불러올 이미지 url
                             .placeholder(defaultImage) // 이미지 로딩 시작하기 전 표시할 이미지
                             .error(defaultImage) // 로딩 에러 발생 시 표시할 이미지
                             .fallback(defaultImage) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                             .into(binding.imageView) // 이미지를 넣을 뷰

                     }else {

                         val url = response.body()!!.data[data_num]!!.img
                         Glide.with(this@SNSDetailsActivity)
                             .load(url) // 불러올 이미지 url
                             .placeholder(defaultImage) // 이미지 로딩 시작하기 전 표시할 이미지
                             .error(defaultImage) // 로딩 에러 발생 시 표시할 이미지
                             .fallback(defaultImage) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                             .into(binding.imageView) // 이미지를 넣을 뷰
                     }



                     val defaultImage2 = R.drawable.profile

                     if(response.body()!!.data[data_num]!!.user.profile==null||response.body()!!.data[data_num]!!.user.profile.equals("")){
                         var url = ""

                         Glide.with(this@SNSDetailsActivity)
                             .load(url) // 불러올 이미지 url
                             .placeholder(defaultImage2) // 이미지 로딩 시작하기 전 표시할 이미지
                             .error(defaultImage2) // 로딩 에러 발생 시 표시할 이미지
                             .fallback(defaultImage2) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                             .circleCrop()
                             .into(binding.profile)

                     }else {

                         var url = response.body()!!.data[data_num]!!.user.profile

                         Glide.with(this@SNSDetailsActivity)
                             .load(url) // 불러올 이미지 url
                             .placeholder(defaultImage2) // 이미지 로딩 시작하기 전 표시할 이미지
                             .error(defaultImage2) // 로딩 에러 발생 시 표시할 이미지
                             .fallback(defaultImage2) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                             .circleCrop()
                             .into(binding.profile)
                     }

                    binding.title.setText(response.body()!!.data[data_num]!!.title)
//                     binding.content.setText(response.body()!!.data[data_num]!!.content)
//                     println(response.body()!!.data[data_num]!!.likes.size)
                    binding.like.setText("いいね！： "+response.body()!!.data[data_num]!!.likes.size.toString())
                     postID = response.body()!!.data[data_num].id
                     println(postID)

                     time = response.body()!!.data[data_num]!!.time.toDouble()
                     calorie = response.body()!!.data[data_num]!!.calorie
                     average_speed = response.body()!!.data[data_num]!!.average_speed
                     altitude = response.body()!!.data[data_num]!!.altitude
                     distance = response.body()!!.data[data_num].distance
                     kind = response.body()!!.data[data_num]!!.kind
                     content = response.body()!!.data[data_num]!!.content
                     username = response.body()!!.data[data_num]!!.user.name




                    userID = response.body()!!.data[data_num]!!.user_id


//                     println("시간 시간 : "+Utils.timeToText(time.toInt()))


                     val date = response.body()!!.data[data_num]!!.created_at // your date
// date is already in Standard ISO format so you don't need custom formatted
//                     val date = "2021-12-16T16:42:00.000000Z" // your date
                     val dateTime : ZonedDateTime = OffsetDateTime.parse(date).toZonedDateTime().plusHours(9)  // parsed date
// format date object to specific format if needed
                     val formatter = DateTimeFormatter.ofPattern("yyyy年 MMM dd日 HH時 mm分 ", Locale.JAPANESE)
                     println( dateTime.format(formatter).toString()) // output : Dec 16, 2021 16:42
//                     yyyy-MM-dd HH:mm:ss z

//                     binding.createdate.setText(dateTime.format(formatter).toString())

                    // 문자열

                     if(time>3600){
                         binding.time.setText("時間 : "+time.toInt()/3600+"時間 "+time/60.toInt()+"分 "+time.toInt()%60+"秒")
                     }else if (time>60){
                         binding.time.setText("時間 : "+time.toInt()/60+"分 "+time.toInt()%60+"秒")
                     }else {
                         binding.time.setText("時間 : "+time.toInt()%60+"秒")
                     }

                     binding.content.setText(content)
                     binding.calorie.setText("カロリー : "+calorie+" Cal")
                     binding.kind.setText("種類 : " + kind)
                     binding.averageSpeed.setText("平均速度 : "+average_speed +" Km/h")
                     binding.altitude.setText("高度 : "+altitude)
                     binding.distance.setText("距離 : "+String.format("%.2f",distance/1000)+" Km")
                     binding.username.setText(username)
                     binding.createdate.setText(dateTime.format(formatter).toString())
                 }  else{
                     println("실패함ㅋㅋ")
                     println(response.body())
                     println(response.message())
                 }
             }

             override fun onFailure(call: Call<SNSResponse>, t: Throwable) {
                 println("아예 가지도 않음ㅋㅋ")
                 println(t.message)
             }
         })

//        binding.backButton.setOnClickListener {
//            finish()
//        }

        binding.likeButton.setOnClickListener{
            supplementService.postLike(token,postID).enqueue(object : Callback<LikeResponse>{
                override fun onResponse(
                    call: Call<LikeResponse>,
                    response: Response<LikeResponse>
                ) {
                    if (response.isSuccessful){
                        Toast.makeText(this@SNSDetailsActivity,"いいねしました",Toast.LENGTH_SHORT).show()
                        supplementService.SNSIndex(token,data_page).enqueue(object : Callback<SNSResponse> {
                            override fun onResponse(call: Call<SNSResponse>, response: Response<SNSResponse>) {

                                if(response.isSuccessful){
                                    binding.like.setText("いいね！："+response.body()!!.data[data_num]!!.likes.size.toString())

                                }  else{
                                    println("실패함ㅋㅋ")
                                    println(response.body())
                                    println(response.message())
                                }
                            }

                            override fun onFailure(call: Call<SNSResponse>, t: Throwable) {
                                println("아예 가지도 않음ㅋㅋ")
                                println(t.message)
                            }
                        })
                    }else {

                        Toast.makeText(this@SNSDetailsActivity,"いいねエラー",Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })
        }

        binding.commentButton.setOnClickListener {

            println("머지? 왜 안날라가")
            val nextIntent = Intent(this, SNSCommentActivity::class.java)
            nextIntent.putExtra("data_num", data_num)
            nextIntent.putExtra("data_page", data_page)
            startActivity(nextIntent)
        }

        binding.followbutton.setOnClickListener {
            supplementService.userFollow(token,userID).enqueue(object : Callback<FollowResponse>{
                override fun onResponse(
                    call: Call<FollowResponse>,
                    response: Response<FollowResponse>
                ) {
                    binding.followbutton.setText("언팔로우")
                }

                override fun onFailure(call: Call<FollowResponse>, t: Throwable) {
                    TODO("Not yet implemented")
                }
            })
        }

}

    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }



}