package com.example.capstonandroid.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstonandroid.R
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.CommentData
import com.example.capstonandroid.network.dto.FollowerData
import kotlinx.android.synthetic.main.comment_item_view.view.*
import kotlinx.android.synthetic.main.comment_item_view.view.user_name
import kotlinx.android.synthetic.main.follower_item_view.view.*
import kotlinx.android.synthetic.main.track_and_name.view.*
import retrofit2.Retrofit
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class RecyclerFollowerAdapter(
    private val items: ArrayList<FollowerData>,
    private val onClick: (FollowerData) -> Unit) : RecyclerView.Adapter<RecyclerFollowerAdapter.ViewHolder>() {

    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    override fun getItemCount(): Int = items.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerFollowerAdapter.ViewHolder, position: Int) {
        val item = items[position]
        val listener = View.OnClickListener { it ->
            item.let {
                onClick(item)
            }
        }
        holder.apply {
            bind(listener, item)
            itemView.tag = item
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.follower_item_view, parent, false)

        return RecyclerFollowerAdapter.ViewHolder(inflatedView)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(listener: View.OnClickListener, item: FollowerData) {


            view.user_name.text = item.username

//            if(item.followcheck === true){
//
//                view.user_followcheck.text = "フォローキャンセル"
//            }else {
//                view.user_followcheck.text = "フォローする"
//            }

            view.setOnClickListener(listener)

            val defaultImage3 = R.drawable.profile
            if(item.profile==null){
                var url = ""

                Glide.with(itemView.context)
                    .load(url) // 불러올 이미지 url
                    .placeholder(defaultImage3) // 이미지 로딩 시작하기 전 표시할 이미지
                    .error(defaultImage3) // 로딩 에러 발생 시 표시할 이미지
                    .fallback(defaultImage3) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                    .circleCrop()
                    .into(itemView.profile_follower)

            }else {

                var url = item.profile

                Glide.with(itemView.context)
                    .load(url) // 불러올 이미지 url
                    .placeholder(defaultImage3) // 이미지 로딩 시작하기 전 표시할 이미지
                    .error(defaultImage3) // 로딩 에러 발생 시 표시할 이미지
                    .fallback(defaultImage3) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                    .circleCrop()
                    .into(itemView.profile_follower)
            }
        }

    }
    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }

}