package com.example.capstonandroid.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.adapter.NotificationRecyclerViewAdapter
import com.example.capstonandroid.databinding.ActivityNotificationBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class NotificationActivity : AppCompatActivity() {

    private var _binding: ActivityNotificationBinding? = null
    private val binding get() = _binding!!

    private  lateinit var retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api
    private lateinit var notificationRecyclerViewItemList: ArrayList<Notification?>
    private lateinit var notificationRecyclerViewAdapter: NotificationRecyclerViewAdapter
    private lateinit var notificationRecyclerView: RecyclerView
    private var isLoading = false // 로딩 중인지
    private var isNext = false // 다음 페이지 있는지
    private var notificationPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "通知"

        initRetrofit()

        CoroutineScope(Dispatchers.Main).launch {
            // 초기화
            notificationPage = 1
            isNext = false
            isLoading = false
            notificationRecyclerViewItemList = ArrayList()

            notificationRecyclerView = binding.recyclerViewNotification

            // 스크롤 리스너 등록
            notificationRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    println("스트롤 함 $isNext $isLoading ${(recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()} ${notificationRecyclerViewItemList.size}")
                    if (isNext) {
                        if (!isLoading) {
                            if (!recyclerView.canScrollVertically(1)) { // 최하단 끝까지 스크롤 했는지 감지
                                println("끝에 옴")
                                getMoreNotifications()
                                isLoading = true
                            }
                        }
                    }
                }
            })

            // 초기값 받아옴
            var token = "Bearer " + getSharedPreferences("other", Context.MODE_PRIVATE).getString("TOKEN","")
            println("홈 프레그먼트$token")
            val getNotificationsResponse = supplementService.getNotifications(token, notificationPage)
            if (getNotificationsResponse.isSuccessful) {
                if (getNotificationsResponse.body()!!.total == 0) {
                    isNext = false
                } else {
                    val notificationList = getNotificationsResponse.body()!!.data
                    for (notification in notificationList) {
                        notificationRecyclerViewItemList.add(notification)
                    }
                    if (getNotificationsResponse.body()!!.next_page_url != null) {
                        notificationPage += 1
                        isNext = true
                    } else {
                        isNext = false
                    }
                }
            }
            notificationRecyclerViewAdapter = NotificationRecyclerViewAdapter(notificationRecyclerViewItemList)
            notificationRecyclerView.adapter = notificationRecyclerViewAdapter

            // 아이템 클릭 리스너 등록
            notificationRecyclerViewAdapter.setOnPostNotificationClickListener(object : NotificationRecyclerViewAdapter.OnPostNotificationClickListener {
                override fun onItemClick(position: Int) {
                    val intent = Intent(this@NotificationActivity, PostActivity::class.java)
                        intent.putExtra("postId", notificationRecyclerViewItemList[position]!!.post_id)
                        startActivity(intent)
                }
            })
            notificationRecyclerViewAdapter.setOnProfileNotificationClickListener(object : NotificationRecyclerViewAdapter.OnProfileNotificationClickListener {
                override fun onItemClick(position: Int) {
                    println("profile 로 이동해야 함")
                }
            })
//            notificationRecyclerViewAdapter.setOnItemClickListener(object : NotificationRecyclerViewAdapter.OnItemClickListener {
//                override fun onItemClick(position: Int) {
//                    when(notificationRecyclerViewItemList[position]!!.not_type) {
//                        "follow", "followRequest" -> {
//                            println("profile 로 넘어가야 함")
//                        }
//                        else -> {
//                            val intent = Intent(this@NotificationActivity, PostActivity::class.java)
//                            intent.putExtra("postId", notificationRecyclerViewItemList[position]!!.post_id)
//                            startActivity(intent)
//                        }
//                    }
//                }
//            })
        }
    }

    private fun getMoreNotifications() {
        val runnable = Runnable {
            notificationRecyclerViewItemList.add(null)
            notificationRecyclerViewAdapter.notifyItemInserted(notificationRecyclerViewItemList.size - 1)
        }
        notificationRecyclerView.post(runnable)

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)

            notificationRecyclerViewItemList.removeAt(notificationRecyclerViewItemList.size - 1)
            notificationRecyclerViewAdapter.notifyItemRemoved(notificationRecyclerViewItemList.size)

            val token = "Bearer " + getSharedPreferences("other", Context.MODE_PRIVATE).getString("TOKEN", "")!!
            val getNotificationsResponse = supplementService.getNotifications(token, notificationPage)

            if (getNotificationsResponse.isSuccessful) {
                val notificationList = getNotificationsResponse.body()!!.data
                for (notification in notificationList) {
                    notificationRecyclerViewItemList.add(notification)
                }

                notificationRecyclerViewAdapter.updateItem(notificationRecyclerViewItemList)
                notificationRecyclerViewAdapter.notifyDataSetChanged()
                if (getNotificationsResponse.body()!!.next_page_url != null) {
                    notificationPage += 1
                    isNext = true
                } else {
                    isNext = false
                }
            }
        }
    }

    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}