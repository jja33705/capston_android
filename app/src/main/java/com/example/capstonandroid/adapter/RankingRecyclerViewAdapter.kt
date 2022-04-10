package com.example.capstonandroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.RankingItem
import com.example.capstonandroid.Utils
import com.example.capstonandroid.databinding.ItemLoadingBinding
import com.example.capstonandroid.databinding.RankingItemBinding

class RankingRecyclerViewAdapter(rankingItemList: ArrayList<RankingItem?>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

    fun updateItem(rankingItemList: ArrayList<RankingItem?>) {
        filteredList = rankingItemList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            RankingViewHolder(RankingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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

    inner class RankingViewHolder(private val binding: RankingItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ranking: RankingItem) {
            binding.tvRanking.text = ranking.ranking.toString()
            binding.tvRankingDate.text = ranking.date
            binding.tvRankingSpeed.text = Utils.formatDoublePointTwo(ranking.speed)
            binding.tvRankingTime.text = Utils.timeToText(ranking.time)
            binding.tvUserName.text = ranking.userName
        }
    }

}