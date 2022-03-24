package com.example.capstonandroid.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.capstonandroid.Utils
import com.example.capstonandroid.databinding.ActivityRankMatchingBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.Post
import com.example.capstonandroid.network.dto.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class RankMatchingActivity : AppCompatActivity() {
    private var _binding: ActivityRankMatchingBinding? = null
    private val binding get() = _binding!!

    private lateinit var retrofit: Retrofit // 레트로핏 인스턴스
    private lateinit var supplementService: BackendApi // api

    private lateinit var trackId: String

    private lateinit var post: Post
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityRankMatchingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRetrofit()

        val intent = intent
        trackId = intent.getStringExtra("trackId")!!
        println("trackId: $trackId")

        val token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!
        CoroutineScope(Dispatchers.Main).launch {
            val rankMatchingResponse = supplementService.rankMatching(token, trackId)
            println(rankMatchingResponse.code())

            val responseIntent = Intent() // 원래 액티비티로 돌아가면서 전달할 데이터를 담을 인텐트

            if (rankMatchingResponse.isSuccessful) {
                if (rankMatchingResponse.code() == 200) {
                    post = rankMatchingResponse.body()!!.post
                    user = rankMatchingResponse.body()!!.user
                    binding.tvVsUserName.text = user.name
                    binding.tvVsTime.text = Utils.timeToText(post.time)
                    binding.tvVsSpeed.text = Utils.avgSpeedToText(post.average_speed)

                    responseIntent.putExtra("matchSuccess", true)
                    responseIntent.putExtra("gpsDataId", post.gps_id)
                    responseIntent.putExtra("postId", post.id)
                } else {
                    Toast.makeText(this@RankMatchingActivity, "적당한 매칭 상대가 없습니다.", Toast.LENGTH_SHORT).show()

                    responseIntent.putExtra("matchSuccess", false)
                }
            }

            delay(3000)

            setResult(SelectMatchTypeActivity.RANK_MATCHING_ACTIVITY_REQUEST_CODE, responseIntent)
            finish()
        }

    }

    // 레트로핏 초기화
    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java)
    }
}