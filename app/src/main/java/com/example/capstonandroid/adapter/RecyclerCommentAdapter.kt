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
import kotlinx.android.synthetic.main.comment_item_view.view.*
import kotlinx.android.synthetic.main.track_and_name.view.*
import retrofit2.Retrofit
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class RecyclerCommentAdapter(
    private val items: ArrayList<CommentData>,
    private val onClick: (CommentData) -> Unit) : RecyclerView.Adapter<RecyclerCommentAdapter.ViewHolder>() {

    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    override fun getItemCount(): Int = items.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerCommentAdapter.ViewHolder, position: Int) {
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
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.comment_item_view, parent, false)

        return RecyclerCommentAdapter.ViewHolder(inflatedView)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(listener: View.OnClickListener, item: CommentData) {


            view.user_name.text = item.username
            view.user_content.text = item.content


            val date = item.created_at // your date
// date is already in Standard ISO format so you don't need custom formatted
//                     val date = "2021-12-16T16:42:00.000000Z" // your date
            val dateTime : ZonedDateTime = OffsetDateTime.parse(date).toZonedDateTime().plusHours(9)  // parsed date
// format date object to specific format if needed
            val formatter = DateTimeFormatter.ofPattern("yyyy年 MMM dd日 HH時 mm分 ", Locale.JAPANESE)
            println( dateTime.format(formatter).toString()) // output : Dec 16, 2021 16:42
//                     yyyy-MM-dd HH:mm:ss z

            val defaultImage3 = R.drawable.profile

            if(item.profile==null){
                var url = ""

                Glide.with(itemView.context)
                    .load(url) // 불러올 이미지 url
                    .placeholder(defaultImage3) // 이미지 로딩 시작하기 전 표시할 이미지
                    .error(defaultImage3) // 로딩 에러 발생 시 표시할 이미지
                    .fallback(defaultImage3) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                    .into(itemView.profile_comment)

            }else {

                var url = item.profile

                Glide.with(itemView.context)
                    .load(url) // 불러올 이미지 url
                    .placeholder(defaultImage3) // 이미지 로딩 시작하기 전 표시할 이미지
                    .error(defaultImage3) // 로딩 에러 발생 시 표시할 이미지
                    .fallback(defaultImage3) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                    .into(itemView.profile_comment)
            }

            view.findViewById<TextView>(R.id.user_created_at).text = dateTime.format(formatter).toString()

            view.deleteButton.setOnClickListener {
                println("안녕하세요 여긴 댓글 삭제 버튼입니다.!  "+item.commentID+"눌렀습니다.")

            }
            view.setOnClickListener(listener)

        }

    }
    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }

}