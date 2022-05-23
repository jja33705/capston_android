package com.example.capstonandroid.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstonandroid.R
import com.example.capstonandroid.Utils
import com.example.capstonandroid.adapter.PostRecyclerViewAdapter
import com.example.capstonandroid.adapter.RankingRecyclerViewAdapter
import com.example.capstonandroid.databinding.ActivityRankingBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class RankingActivity : AppCompatActivity() {
    private var _binding: ActivityRankingBinding? = null
    private val binding get() = _binding!!

    private lateinit var trackId: String

    private lateinit var retrofit: Retrofit // 레트로핏 인스턴스
    private lateinit var supplementService: BackendApi // api

    private var page = 1 // 현재 페이지
    private var isNext = false // 다음 페이지 있는지

    private var isLoading = false

    private lateinit var rankingItemList: ArrayList<Post?>
    private lateinit var rankingRecyclerViewAdapter: RankingRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRankingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "ランキング"

        var intent = intent
        trackId = intent.getStringExtra("trackId")!!

        initRetrofit()

        // 처음 1페이지 랭킹 받아오기
        initRankingList()

        // 스크롤 리스너
        binding.recyclerViewRanking.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isNext) {
                    if (!isLoading) {
                        // 마지막거까지 다 보이면
                        if ((recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition() == rankingItemList.size - 1) {
                            getMoreRankings()
                            isLoading = true
                        }
                    }
                }
            }
        })
    }

    private fun initRankingList() {
        CoroutineScope(Dispatchers.Main).launch {
            rankingItemList = ArrayList()

            val token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!
            val rankingResponse = supplementService.getRanking(token, trackId, page)

            if (rankingResponse.isSuccessful) {
                when (rankingResponse.code()) {
                    200 -> {
                        println("초기 랭킹: ${rankingResponse.body()}")
                        println("초기 개수: ${rankingResponse.body()!!.data.size}")
                        for (rankingItem in rankingResponse.body()!!.data) {
                            rankingItemList.add(rankingItem)
                        }

                        binding.trackRankFirstName.text = rankingItemList[0]!!.user.name
                        binding.trackRankFirstTime.text = Utils.timeToText(rankingItemList[0]!!.time)

                        val defaultImage = R.drawable.profile
                        val profileImageUrl = rankingItemList[0]!!.user.profile

                        Glide.with(this@RankingActivity)
                            .load(profileImageUrl) // 불러올 이미지 url
                            .placeholder(defaultImage) // 이미지 로딩 시작하기 전 표시할 이미지
                            .error(defaultImage) // 로딩 에러 발생 시 표시할 이미지
                            .fallback(defaultImage) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                            .circleCrop()
                            .into(binding.trackRankFirstUserImage)

                        if (rankingResponse.body()!!.next_page_url != null) {
                            page += 1

                            isNext = true
                        } else {
                            isNext = false
                        }
                    }
                    204 -> {
                        isNext = false
                        binding.trackRankFirstLinearLayout.visibility = View.GONE
                    }
                }

                // 리사이클러뷰 설정
                rankingRecyclerViewAdapter = RankingRecyclerViewAdapter(rankingItemList)
                binding.recyclerViewRanking.adapter = rankingRecyclerViewAdapter

                // 아이템 클릭 리스너 등록
                rankingRecyclerViewAdapter.setOnItemClickListener(object : RankingRecyclerViewAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        val intent = Intent(this@RankingActivity, PostActivity::class.java)
                        intent.putExtra("postId", rankingItemList[position]!!.id)
                        intent.putExtra("postKind", 1)
                        startActivity(intent)
                    }
                })
            }
        }
    }

    private fun getMoreRankings() {
        val runnable = Runnable {
            rankingItemList.add(null)
            rankingRecyclerViewAdapter.notifyItemInserted(rankingItemList.size - 1)
        }
        binding.recyclerViewRanking.post(runnable)

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)

            rankingItemList.removeAt(rankingItemList.size - 1)
            rankingRecyclerViewAdapter.notifyItemRemoved(rankingItemList.size)

            val token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!
            val rankingResponse = supplementService.getRanking(token, trackId, page)

            if (rankingResponse.isSuccessful) {
                when (rankingResponse.code()) {
                    200 -> {
                        println("초기 랭킹: ${rankingResponse.body()}")
                        for (rankingItem in rankingResponse.body()!!.data) {
                            rankingItemList.add(rankingItem)
                        }
                        rankingRecyclerViewAdapter.notifyItemRangeInserted((page - 1) * rankingResponse.body()!!.per_page, rankingResponse.body()!!.to)
                        isLoading = false
                        if (rankingResponse.body()!!.next_page_url != null) {
                            page += 1
                            isNext = true
                        } else {
                            isNext = false
                        }
                    }
                    204 -> {
                        isNext = false
                    }
                }
            }
        }
    }

    // 레트로핏 초기화
    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}