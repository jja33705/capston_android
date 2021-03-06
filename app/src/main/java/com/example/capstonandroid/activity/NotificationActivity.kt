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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "通知"

        initRetrofit()

        notificationRecyclerView = binding.recyclerViewNotification

        notificationRecyclerViewItemList = ArrayList()
        notificationRecyclerViewAdapter = NotificationRecyclerViewAdapter(notificationRecyclerViewItemList)
        notificationRecyclerView.adapter = notificationRecyclerViewAdapter

        // 아이템 클릭 리스너 등록
        notificationRecyclerViewAdapter.setOnPostNotificationClickListener(object : NotificationRecyclerViewAdapter.OnClickPostNotificationListener {
            override fun onItemClick(position: Int) {
                val intent = Intent(this@NotificationActivity, PostActivity::class.java)
                intent.putExtra("postId", notificationRecyclerViewItemList[position]!!.post_id)
                startActivity(intent)
            }
        })
        notificationRecyclerViewAdapter.setOnProfileNotificationClickListener(object : NotificationRecyclerViewAdapter.OnClickProfileNotificationListener {
            override fun onItemClick(position: Int) {
                println("profile 로 이동해야 함")
                val intent = Intent(this@NotificationActivity, ProfileActivity::class.java)
                intent.putExtra("userId", notificationRecyclerViewItemList[position]!!.target_mem_id)
                startActivity(intent)
            }
        })
        notificationRecyclerViewAdapter.setOnClickAcceptFollowRequestListener(object : NotificationRecyclerViewAdapter.OnClickAcceptFollowRequestListener {
            override fun onItemClick(position: Int) {
                CoroutineScope(Dispatchers.Main).launch {
                    val token = "Bearer ${getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")}"
                    val followResponse = supplementService.follow(token, notificationRecyclerViewItemList[position]!!.target_mem_id)
                    if (followResponse.isSuccessful) {
                        deleteNotification(position)
                    }
                }

            }
        })
        notificationRecyclerViewAdapter.setOnClickDeleteFollowRequestListener(object : NotificationRecyclerViewAdapter.OnClickDeleteFollowRequestListener {
            override fun onItemClick(position: Int) {
                CoroutineScope(Dispatchers.Main).launch {
                    deleteNotification(position)
                }
            }

        })

        initRecyclerViewData()

        // 아래로 슬라이드했을 때 새로고침
        binding.swipeRefreshLayoutNotification.setOnRefreshListener {
            initRecyclerViewData()
            binding.swipeRefreshLayoutNotification.isRefreshing = false
        }
    }

    private fun initRecyclerViewData() {
        CoroutineScope(Dispatchers.Main).launch {
            // 초기화
            notificationRecyclerViewItemList.clear()

            // 초기값 받아옴
            var token = "Bearer " + getSharedPreferences("other", Context.MODE_PRIVATE).getString("TOKEN","")
            println("홈 프레그먼트$token")
            val getNotificationsResponse = supplementService.getNotifications(token)
            if (getNotificationsResponse.isSuccessful) {
                if (getNotificationsResponse.body()!!.isNotEmpty()) {
                    for (notification in getNotificationsResponse.body()!!) {
                        notificationRecyclerViewItemList.add(notification)
                    }
                    notificationRecyclerViewAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    suspend fun deleteNotification(position: Int) {
        val token = "Bearer ${getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")}"
        val deleteNotificationResponse = supplementService.deleteNotification(token, notificationRecyclerViewItemList[position]!!.not_id)
        if (deleteNotificationResponse.isSuccessful) {
            notificationRecyclerViewItemList.removeAt(position)
            notificationRecyclerViewAdapter.notifyItemRemoved(position)
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