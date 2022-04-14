package com.example.capstonandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstonandroid.R
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.UserData
import kotlinx.android.synthetic.main.item_view.view.*
import kotlinx.android.synthetic.main.track_and_name.view.*
import kotlinx.android.synthetic.main.track_and_name.view.imageView
import kotlinx.android.synthetic.main.view_item_layout.view.*
import retrofit2.Retrofit

// 여기가 SNS쪽
class RecyclerUserAdapter2(
    private val items: ArrayList<UserData>,
    private val onClick: (UserData) -> Unit) : RecyclerView.Adapter<RecyclerUserAdapter2.ViewHolder>() {

    override fun getItemCount(): Int = items.size

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
        fun bind(listener: View.OnClickListener, item: UserData) {
            view.findViewById<TextView>(R.id.txtUser_title).text = item.name
            view.findViewById<TextView>(R.id.txtUser_name).text = item.title
            view.findViewById<TextView>(R.id.txtUser_created_id).text = item.created_id

            view.setOnClickListener(listener)
            val defaultImage = R.drawable.map
            val url = item.map_image[0].url

            Glide.with(itemView.context)
                .load(url) // 불러올 이미지 url
                .placeholder(defaultImage) // 이미지 로딩 시작하기 전 표시할 이미지
                .error(defaultImage) // 로딩 에러 발생 시 표시할 이미지
                .fallback(defaultImage) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                .into(itemView.imageView)


        }
    }
}