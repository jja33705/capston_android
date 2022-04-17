package com.example.capstonandroid.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstonandroid.R
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.UserData
import kotlinx.android.synthetic.main.item_view.view.*
import kotlinx.android.synthetic.main.item_view2.view.*
import kotlinx.android.synthetic.main.track_and_name.view.*
import kotlinx.android.synthetic.main.track_and_name.view.imageView
import kotlinx.android.synthetic.main.view_item_layout.view.*
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

// 여기가 SNS쪽
class RecyclerUserAdapter2(
    private val items: ArrayList<UserData>,
    private val onClick: (UserData) -> Unit) : RecyclerView.Adapter<RecyclerUserAdapter2.ViewHolder>() {

    override fun getItemCount(): Int = items.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerUserAdapter2.ViewHolder, position: Int) {
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
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.item_view2, parent, false)
        return RecyclerUserAdapter2.ViewHolder(inflatedView)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(listener: View.OnClickListener, item: UserData) {
            view.findViewById<TextView>(R.id.txtUser_title).text = item.name
            view.findViewById<TextView>(R.id.txtUser_name).text = item.title

            println(item.created_id)

            val date = item.created_id // your date
// date is already in Standard ISO format so you don't need custom formatted
//                     val date = "2021-12-16T16:42:00.000000Z" // your date
            val dateTime : ZonedDateTime = OffsetDateTime.parse(date).toZonedDateTime().plusHours(9)  // parsed date
// format date object to specific format if needed
            val formatter = DateTimeFormatter.ofPattern("yyyy年 MMM dd日 HH時 mm分 ", Locale.JAPANESE)
            println( dateTime.format(formatter).toString()) // output : Dec 16, 2021 16:42
//                     yyyy-MM-dd HH:mm:ss z

            view.findViewById<TextView>(R.id.txtUser_created_id).text = dateTime.format(formatter).toString()


            view.setOnClickListener(listener)

            val defaultImage = R.drawable.map

            if(item.map_image==null){
                var url = ""

                Glide.with(itemView.context)
                    .load(url) // 불러올 이미지 url
                    .placeholder(defaultImage) // 이미지 로딩 시작하기 전 표시할 이미지
                    .error(defaultImage) // 로딩 에러 발생 시 표시할 이미지
                    .fallback(defaultImage) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                    .into(itemView.imageView)

            }else {

                var url = item.map_image

                Glide.with(itemView.context)
                    .load(url) // 불러올 이미지 url
                    .placeholder(defaultImage) // 이미지 로딩 시작하기 전 표시할 이미지
                    .error(defaultImage) // 로딩 에러 발생 시 표시할 이미지
                    .fallback(defaultImage) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                    .into(itemView.imageView)
            }

            val defaultImage2 = R.drawable.profile

            if(item.profile==null||item.profile.equals("")){
                var url = ""

                Glide.with(itemView.context)
                    .load(url) // 불러올 이미지 url
                    .placeholder(defaultImage2) // 이미지 로딩 시작하기 전 표시할 이미지
                    .error(defaultImage2) // 로딩 에러 발생 시 표시할 이미지
                    .fallback(defaultImage2) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                    .circleCrop()
                    .into(itemView.userImage)

            }else {

                var url = item.profile

                Glide.with(itemView.context)
                    .load(url) // 불러올 이미지 url
                    .placeholder(defaultImage2) // 이미지 로딩 시작하기 전 표시할 이미지
                    .error(defaultImage2) // 로딩 에러 발생 시 표시할 이미지
                    .fallback(defaultImage2) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                    .circleCrop()
                    .into(itemView.userImage)
            }
        }
    }
}