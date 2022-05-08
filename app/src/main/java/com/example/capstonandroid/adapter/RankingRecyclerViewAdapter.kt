package com.example.capstonandroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.Utils
import com.example.capstonandroid.databinding.ItemLoadingBinding
import com.example.capstonandroid.databinding.RankingRecyclerViewItemBinding
import com.example.capstonandroid.network.dto.Post

class RankingRecyclerViewAdapter(rankingItemList: ArrayList<Post?>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var filteredList = rankingItemList

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            RankingViewHolder(RankingRecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            LoadingViewHolder(ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RankingViewHolder) {
            val item = filteredList[position]!!
            val rankingViewHolder = holder as RankingViewHolder
            rankingViewHolder.bind(item)
        } else if (holder is LoadingViewHolder) {

        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    inner class LoadingViewHolder(var binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    inner class RankingViewHolder(private val binding: RankingRecyclerViewItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ranking: Post) {
            binding.tvRanking.text = (adapterPosition + 1).toString()
            binding.tvRankingDate.text = ranking.date
            binding.tvRankingSpeed.text = "${Utils.formatDoublePointTwo(ranking.average_speed)}km/h"
            binding.tvRankingTime.text = Utils.timeToText(ranking.time)
            binding.tvUserName.text = ranking.user.name
        }
    }

}