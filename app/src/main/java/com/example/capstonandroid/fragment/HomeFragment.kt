package com.example.capstonandroid.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.activity.PostActivity
import com.example.capstonandroid.adapter.*
import com.example.capstonandroid.databinding.FragmentHomeBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class HomeFragment : Fragment()  {

    private var mBinding: FragmentHomeBinding? = null
    private val binding get() = mBinding!!

    private  lateinit var retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    private lateinit var postRecyclerViewItemList: ArrayList<Post?>
    private lateinit var postRecyclerViewAdapter: PostRecyclerViewAdapter
    private lateinit var postRecyclerView: RecyclerView
    private var isLoading = false // 로딩 중인지
    private var isNext = false // 다음 페이지 있는지
    private var postPage = 1      // 현재 페이지

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 함수 초기화
        initRetrofit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("HomeFragment 시작됨")

    }

    private fun initRecyclerViewData() {
        CoroutineScope(Dispatchers.Main).launch {
            // 초기화
            postPage = 1
            isNext = false
            isLoading = false
            postRecyclerViewItemList.clear()

            // 초기값 받아옴
            var token = "Bearer " + requireActivity().getSharedPreferences("other", Context.MODE_PRIVATE).getString("TOKEN","")
            val getPostsResponse = supplementService.getPosts(token, postPage)
            if (getPostsResponse.isSuccessful) {
                if (getPostsResponse.body()!!.total == 0) {
                    isNext = false
                } else {
                    val postList = getPostsResponse.body()!!.data
                    for (post in postList) {
                        postRecyclerViewItemList.add(post)
                    }
                    postRecyclerViewAdapter.notifyDataSetChanged()
                    if (getPostsResponse.body()!!.next_page_url != null) {
                        postPage += 1
                        isNext = true
                    } else {
                        isNext = false
                    }
                }
            }
        }
    }

    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }

    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        println("HomeFragment: onStart 호출")

        postRecyclerView = binding.recyclerViewPost

        // 스크롤 리스너 등록
        postRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                println("스트롤 함 $isNext $isLoading ${(recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()} ${postRecyclerViewItemList.size}")
                if (isNext) {
                    if (!isLoading) {
                        if (!recyclerView.canScrollVertically(1)) { // 최하단 끝까지 스크롤 했는지 감지
                            println("끝에 옴")
                            getMorePosts()
                            isLoading = true
                        }
                    }
                }
            }
        })

        postRecyclerViewItemList = ArrayList()
        postRecyclerViewAdapter = PostRecyclerViewAdapter(postRecyclerViewItemList)
        postRecyclerView.adapter = postRecyclerViewAdapter

        // 아이템 클릭 리스너 등록
        postRecyclerViewAdapter.setOnItemClickListener(object : PostRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val intent = Intent(activity, PostActivity::class.java)
                intent.putExtra("postId", postRecyclerViewItemList[position]!!.id)
                intent.putExtra("postKind", 1)
                startActivity(intent)
            }
        })

        initRecyclerViewData()

        binding.swipeRefreshLayoutPost.setOnRefreshListener {
            initRecyclerViewData()
            binding.swipeRefreshLayoutPost.isRefreshing = false
        }
    }

    private fun getMorePosts() {
        val runnable = Runnable {
            postRecyclerViewItemList.add(null)
            postRecyclerViewAdapter.notifyItemInserted(postRecyclerViewItemList.size - 1)
        }
        postRecyclerView.post(runnable)

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)

            postRecyclerViewItemList.removeAt(postRecyclerViewItemList.size - 1)
            postRecyclerViewAdapter.notifyItemRemoved(postRecyclerViewItemList.size)

            val token = "Bearer " + requireActivity().getSharedPreferences("other", Context.MODE_PRIVATE).getString("TOKEN", "")!!
            val getPostsResponse = supplementService.getPosts(token, postPage)

            if (getPostsResponse.isSuccessful) {
                val postList = getPostsResponse.body()!!.data
                for (post in postList) {
                    postRecyclerViewItemList.add(post)
                }
                postRecyclerViewAdapter.notifyItemRangeInserted((postPage - 1) * getPostsResponse.body()!!.per_page, getPostsResponse.body()!!.to)
                isLoading = false
                if (getPostsResponse.body()!!.next_page_url != null) {
                    postPage += 1
                    isNext = true
                } else {
                    isNext = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        println("HomeFragment: onResume 호출")
    }

}
