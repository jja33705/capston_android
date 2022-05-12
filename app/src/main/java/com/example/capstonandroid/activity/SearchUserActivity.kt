package com.example.capstonandroid.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.adapter.SearchUserRecyclerViewAdapter
import com.example.capstonandroid.databinding.ActivitySearchUserBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit


class SearchUserActivity : AppCompatActivity() {

    private var _binding: ActivitySearchUserBinding? = null
    private val binding get() = _binding!!

    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    private lateinit var userRecyclerViewItemList: ArrayList<User?>
    private lateinit var userRecyclerViewAdapter: SearchUserRecyclerViewAdapter
    private lateinit var userRecyclerView: RecyclerView
    private var page = 1
    private var isNext = false
    private var isLoading = false

    private var keyword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivitySearchUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRetrofit()

        userRecyclerView = binding.recyclerViewSearchUser

        // 스크롤 리스너 등록
        userRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isNext) {
                    if (!isLoading) {
                        if (!recyclerView.canScrollVertically(1)) { // 최하단 끝까지 스크롤 했는지 감지
                            println("끝에 옴")
                            getMoreUsers()
                            isLoading = true
                        }
                    }
                }
            }
        })

        userRecyclerViewItemList = ArrayList()
        userRecyclerViewAdapter = SearchUserRecyclerViewAdapter(userRecyclerViewItemList)
        userRecyclerView.adapter = userRecyclerViewAdapter

        userRecyclerViewAdapter.setOnItemClickListener(object: SearchUserRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val intent = Intent(this@SearchUserActivity, ProfileActivity::class.java)
                intent.putExtra("userId", userRecyclerViewItemList[position]!!.id)
                startActivity(intent)
            }
        })

        binding.btnSearch.setOnClickListener {
            keyword = binding.etSearchKeyword.text.toString()
            searchUser()
        }
    }

    private fun searchUser() {
        CoroutineScope(Dispatchers.Main).launch {
            // 초기화
            page = 1
            isNext = false
            isLoading = false
            userRecyclerViewItemList.clear()

            // 초기값 받아옴
            var token = "Bearer ${getSharedPreferences("other", Context.MODE_PRIVATE).getString("TOKEN","")}"
            val searchUserResponse = supplementService.searchUser(token, keyword, page)
            if (searchUserResponse.isSuccessful) {
                if (searchUserResponse.body()!!.total == 0) {
                    isNext = false
                } else {
                    val userList = searchUserResponse.body()!!.data
                    for (user in userList) {
                        userRecyclerViewItemList.add(user)
                    }
                    userRecyclerViewAdapter.notifyDataSetChanged()
                    if (searchUserResponse.body()!!.next_page_url != null) {
                        page += 1
                        isNext = true
                    } else {
                        isNext = false
                    }
                }
            }
        }
    }

    private fun getMoreUsers() {
        val runnable = Runnable {
            userRecyclerViewItemList.add(null)
            userRecyclerViewAdapter.notifyItemInserted(userRecyclerViewItemList.size - 1)
        }
        userRecyclerView.post(runnable)

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)

            userRecyclerViewItemList.removeAt(userRecyclerViewItemList.size - 1)
            userRecyclerViewAdapter.notifyItemRemoved(userRecyclerViewItemList.size)

            val token = "Bearer ${getSharedPreferences("other", Context.MODE_PRIVATE).getString("TOKEN", "")!!}"
            val searchUserResponse = supplementService.searchUser(token, keyword, page)

            if (searchUserResponse.isSuccessful) {
                val userList = searchUserResponse.body()!!.data
                for (user in userList) {
                    userRecyclerViewItemList.add(user)
                }
                userRecyclerViewAdapter.notifyItemRangeInserted((page - 1) * searchUserResponse.body()!!.per_page, searchUserResponse.body()!!.to)
                isLoading = false
                if (searchUserResponse.body()!!.next_page_url != null) {
                    page += 1
                    isNext = true
                } else {
                    isNext = false
                }
            }
        }
    }

    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}