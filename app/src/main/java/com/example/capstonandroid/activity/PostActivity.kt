package com.example.capstonandroid.activity

// SNS 누르면 자세히 뜨는 것

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstonandroid.R
import com.example.capstonandroid.Utils
import com.example.capstonandroid.adapter.CommentRecyclerViewAdapter
import com.example.capstonandroid.adapter.PostRecyclerViewAdapter
import com.example.capstonandroid.databinding.ActivityPostBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class PostActivity : AppCompatActivity() {
    private lateinit var retrofit: Retrofit  //레트로핏
    private lateinit var supplementService: BackendApi // api

    private lateinit var post: Post

    private var _binding: ActivityPostBinding? = null
    private val binding get() = _binding!!


    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "活動"

        initRetrofit()

        val intent = intent
        var postId = intent.getIntExtra("postId", -1)

        var token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")
        CoroutineScope(Dispatchers.Main).launch {
            val getPostResponse = supplementService.getPost(token, postId)
            if (getPostResponse.isSuccessful) {
                post = getPostResponse.body()!!

                val defaultImage = R.drawable.map
                val mapImageUrl = post.img
                Glide.with(this@PostActivity)
                    .load(mapImageUrl)
                    .placeholder(defaultImage)
                    .error(defaultImage)
                    .fallback(defaultImage)
                    .into(binding.imageViewMapImage)

                binding.title.text = post.title
                binding.content.text = post.content
                binding.time.text = Utils.timeToStringText(post.time)
                binding.calorie.text = "カロリー : ${post.calorie}Kcal"
                binding.averageSpeed.text = "平均速度 : ${post.average_speed}Km/h"
                binding.altitude.text = "累積高度 : ${String.format("%.0f", post.altitude)}m"
                binding.distance.text = "累積距離 : ${String.format("%.2f", post.distance)}Km"
                binding.username.text = post.user.name

                if (post.likeCheck) {
                    binding.likeButton.setImageResource(R.drawable.like_new2)
                }

                binding.like.text = "いいね！： ${post.likes.size}"

                val date = post.created_at // your date
                // date is already in Standard ISO format so you don't need custom formatted
                //                     val date = "2021-12-16T16:42:00.000000Z" // your date
                val dateTime: ZonedDateTime =
                    OffsetDateTime.parse(date).toZonedDateTime().plusHours(9)  // parsed date
                // format date object to specific format if needed
                val formatter =
                    DateTimeFormatter.ofPattern("yyyy年 MMM dd日 HH時 mm分 ", Locale.JAPANESE)
                println(dateTime.format(formatter).toString()) // output : Dec 16, 2021 16:42
                binding.createdate.text = dateTime.format(formatter).toString()

                binding.kind.text = when (post.kind) {
                    "싱글" -> "種類 : シングル"
                    "친선" -> "種類 : 練習"
                    "랭크" -> "種類 : ランク"
                    else -> "種類 : 自由"
                }

                if (post.opponent_post != null) {
                    binding.tvOpponent.text =
                        "${post.opponent_post!!.user.name}様の「${post.opponent_post!!.title}」と一緒に走りました！"
                    binding.tvOpponent.visibility = View.VISIBLE
                }
            }
        }

            binding.likeButton.setOnClickListener {
//            supplementService.postLike(token, postID).enqueue(object : Callback<LikeResponse> {
//                override fun onResponse(
//                    call: Call<LikeResponse>,
//                    response: Response<LikeResponse>
//                ) {
//                    if (response.isSuccessful) {
//                        Toast.makeText(this@PostActivity, "いいねしました", Toast.LENGTH_SHORT).show()
////                        supplementService.SNSIndex(token,data_page).enqueue(object : Callback<GetPostsResponse> {
////                            override fun onResponse(call: Call<GetPostsResponse>, response: Response<GetPostsResponse>) {
////
////                                if(response.isSuccessful){
////                                    binding.like.setText("いいね！："+response.body()!!.data[data_num]!!.likes.size.toString())
////
////                                    if(response.body()!!.data[data_num].likeCheck===true){
////                                        binding.likeButton.setImageResource(R.drawable.like_new2)
////                                    }else{
////                                        binding.likeButton.setImageResource(R.drawable.like_new3)
////                                    }
////                                }  else{
////                                    println("실패함ㅋㅋ")
////                                    println(response.body())
////                                    println(response.message())
////                                }
////
////                            }
////
////                            override fun onFailure(call: Call<GetPostsResponse>, t: Throwable) {
////                                println("아예 가지도 않음ㅋㅋ")
////                                println(t.message)
////                            }
////                        })
//                    } else {
//
//                        Toast.makeText(this@PostActivity, "いいね", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
//                }
//
//            })
            }

            binding.commentButton.setOnClickListener {

//            supplementService.commentIndex(token,).enqueue(object : Callback<CommentIndexResponse>{
//                override fun onResponse(
//                    call: Call<CommentIndexResponse>,
//                    response: Response<CommentIndexResponse>
//                ) {
//                    if(response.isSuccessful){
//
//                    }else {
//
//                    }
//                }
//
//                override fun onFailure(call: Call<CommentIndexResponse>, t: Throwable) {
//                }
//
//            })


//
//            println("머지? 왜 안날라가")
//            val nextIntent = Intent(this, SNSCommentActivity::class.java)
//            nextIntent.putExtra("data_num", data_num)
//            nextIntent.putExtra("data_page", data_page)
//            startActivity(nextIntent)
            }

            binding.followbutton.setOnClickListener {
//            supplementService.userFollow(token, userID).enqueue(object : Callback<FollowResponse> {
//                override fun onResponse(
//                    call: Call<FollowResponse>,
//                    response: Response<FollowResponse>
//                ) {
//                    binding.followbutton.setText("언팔로우")
//                }
//
//                override fun onFailure(call: Call<FollowResponse>, t: Throwable) {\
//                }
//            })
            }

        }

        private fun initRetrofit() {
            retrofit = RetrofitClient.getInstance()
            supplementService = retrofit.create(BackendApi::class.java);
        }

        override fun onDestroy() {
            super.onDestroy()
            _binding = null
        }

        override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
            return super.onCreateView(name, context, attrs)

        }
    }
