package com.example.capstonandroid.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ItemLoadingBinding
import com.example.capstonandroid.databinding.SearchUserRecyclerViewItemBinding
import com.example.capstonandroid.network.dto.User
import kotlin.collections.ArrayList

class SearchUserRecyclerViewAdapter(userRecyclerViewItemList: ArrayList<User?>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_LOADING = 1
    }

    private var filteredList = userRecyclerViewItemList
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
            UserRecyclerViewViewHolder(SearchUserRecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            LoadingViewHolder(ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UserRecyclerViewViewHolder) {
            val item = filteredList[position]!!
            holder.bind(item)
        } else if (holder is LoadingViewHolder) {

        }
    }

    inner class LoadingViewHolder(private var binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    inner class UserRecyclerViewViewHolder(private var binding: SearchUserRecyclerViewItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.linearLayoutSearchUserRecyclerViewItem.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && filteredList[position] != null &&mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position)
                }
            }
        }

        @SuppressLint("NewApi")
        fun bind(userRecyclerViewItem: User) {
            binding.textViewName.text = userRecyclerViewItem.name

            val defaultImage = R.drawable.follower

            Glide.with(itemView.context)
                .load(userRecyclerViewItem.profile) // 불러올 이미지 url
                .placeholder(defaultImage) // 이미지 로딩 시작하기 전 표시할 이미지
                .error(defaultImage) // 로딩 에러 발생 시 표시할 이미지
                .fallback(defaultImage) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                .into(binding.circleImageViewProfileImage)
        }
    }
}