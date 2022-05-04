package com.example.capstonandroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.Utils
import com.example.capstonandroid.databinding.FriendlyMatchingItemBinding
import com.example.capstonandroid.databinding.ItemLoadingBinding
import com.example.capstonandroid.network.dto.Post

class FriendlyMatchingRecyclerViewAdapter(friendlyMatchingItemList: ArrayList<Post?>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 아이템 클릭 이벤트 처리를 위한 커스텀 리스너
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var filteredList = friendlyMatchingItemList
    private lateinit var mOnItemClickListener: OnItemClickListener

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_LOADING = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (filteredList[position]) {
            null -> TYPE_LOADING
            else -> TYPE_ITEM
        }
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        mOnItemClickListener = onItemClickListener
    }

    fun updateItem(friendlyMatchingItemList: ArrayList<Post?>) {
        filteredList = friendlyMatchingItemList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            FriendlyMatchingViewHolder(FriendlyMatchingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            LoadingViewHolder(ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FriendlyMatchingViewHolder) {
            val item = filteredList[position]!!
            holder.bind(item)
        } else if (holder is LoadingViewHolder) {

        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    inner class LoadingViewHolder(var binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    inner class FriendlyMatchingViewHolder(private val binding: FriendlyMatchingItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            // 클릭 시 리스너 함수 호출
            binding.friendlyMatchItemLinearLayout.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && filteredList[position] != null &&mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position)
                }
            }
        }
        fun bind(friendlyMatching: Post) {
            binding.tvPostTitle.text = friendlyMatching.title
            binding.tvDate.text = friendlyMatching.date
            binding.tvSpeed.text = "${Utils.formatDoublePointTwo(friendlyMatching.average_speed)}km/h"
            binding.tvTime.text = Utils.timeToText(friendlyMatching.time)
            binding.tvUserName.text = friendlyMatching.user.name
        }
    }
}