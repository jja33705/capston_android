package com.example.capstonandroid.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstonandroid.R
import com.example.capstonandroid.activity.PostActivity
import com.example.capstonandroid.databinding.ItemLoadingBinding
import com.example.capstonandroid.databinding.PostRecyclerViewItemBinding
import com.example.capstonandroid.network.dto.Post
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class PostRecyclerViewAdapter(postRecyclerViewItemList: ArrayList<Post?>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_LOADING = 1
    }

    private var filteredList = postRecyclerViewItemList
    private lateinit var  mOnItemClickListener: OnItemClickListener

    override fun getItemViewType(position: Int): Int {
        return when (filteredList[position]) {
            null -> TYPE_LOADING
            else -> TYPE_ITEM
        }
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        mOnItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            PostRecyclerViewViewHolder(PostRecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            LoadingViewHolder(ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PostRecyclerViewViewHolder) {
            val item = filteredList[position]!!
            holder.bind(item)
        } else if (holder is LoadingViewHolder) {

        }
    }

    inner class LoadingViewHolder(private var binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    inner class PostRecyclerViewViewHolder(private var binding: PostRecyclerViewItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.linearLayoutPostRecyclerViewItem.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && filteredList[position] != null &&mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position)
                }
            }
        }

        @SuppressLint("NewApi")
        fun bind(postRecyclerViewItem: Post) {
            binding.tvTitle.text = postRecyclerViewItem.title
            binding.tvUserName.text = postRecyclerViewItem.user.name
            var user_mmr = postRecyclerViewItem.user.mmr

            println("유저 MMR" + user_mmr.toString())
            if(user_mmr!! >= 0&& user_mmr!! <= 99){
                binding.medalLayout.setBackgroundResource(R.drawable.medal_bronze)
            }else if (user_mmr >= 100 && user_mmr <= 199){
                binding.medalLayout.setBackgroundResource(R.drawable.medal_silver)
            }else if (user_mmr >= 200){
                binding.medalLayout.setBackgroundResource(R.drawable.medal_gold)
            }
            // 타임포멧
            val date = postRecyclerViewItem.created_at // your date
// date is already in Standard ISO format so you don't need custom formatted
//                     val date = "2021-12-16T16:42:00.000000Z" // your date
            val dateTime : ZonedDateTime = OffsetDateTime.parse(date).toZonedDateTime().plusHours(9)  // parsed date
// format date object to specific format if needed
            val formatter = DateTimeFormatter.ofPattern("yyyy年 MMM dd日 HH時 mm分 ", Locale.JAPANESE)
            println( dateTime.format(formatter).toString()) // output : Dec 16, 2021 16:42
//                     yyyy-MM-dd HH:mm:ss z

            binding.tvCreatedAt.text = dateTime.format(formatter)
            binding.tvLikeCount.text = " ${postRecyclerViewItem.likes.size}"

            val defaultImage = R.drawable.map
            val defaultImage2 = R.drawable.profile
            // 맵 이미지 띄움
            val mapImageUrl = postRecyclerViewItem.img
            Glide.with(itemView.context)
                .load(mapImageUrl)
                .placeholder(defaultImage)
                .error(defaultImage)
                .fallback(defaultImage)
                .into(binding.imageViewMap)

            val profileImageUrl = postRecyclerViewItem.user.profile

            Glide.with(itemView.context)
                    .load(profileImageUrl) // 불러올 이미지 url
                    .placeholder(defaultImage2) // 이미지 로딩 시작하기 전 표시할 이미지
                    .error(defaultImage2) // 로딩 에러 발생 시 표시할 이미지
                    .fallback(defaultImage2) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                    .circleCrop()
                    .into(binding.userImage)

        }
    }
}