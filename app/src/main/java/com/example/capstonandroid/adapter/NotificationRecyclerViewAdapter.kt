package com.example.capstonandroid.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.adapter.NotificationRecyclerViewAdapter.Companion.TYPE_LOADING
import com.example.capstonandroid.databinding.FollowRequestRecyclerViewItemBinding
import com.example.capstonandroid.databinding.ItemLoadingBinding
import com.example.capstonandroid.databinding.NotificationRecyclerViewItemBinding
import com.example.capstonandroid.network.dto.Notification
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class NotificationRecyclerViewAdapter(notificationRecyclerViewItemList: ArrayList<Notification?>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnPostNotificationClickListener {
        fun onItemClick(position: Int)
    }

    interface OnProfileNotificationClickListener {
        fun onItemClick(position: Int)
    }

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_FOLLOW_REQUEST = 1
        private const val TYPE_LOADING = 2
    }

    private var filteredList = notificationRecyclerViewItemList
    private lateinit var  mOnPostNotificationClickListener: OnPostNotificationClickListener
    private lateinit var  mOnProfileNotificationClickListener: OnProfileNotificationClickListener

    override fun getItemViewType(position: Int): Int {
        return if (filteredList[position] == null) {
            TYPE_LOADING
        } else {
            when (filteredList[position]?.not_type) {
                "followRequest" -> {
                    TYPE_FOLLOW_REQUEST
                }
                else -> {
                    TYPE_ITEM
                }
            }
        }
    }

    fun setOnPostNotificationClickListener(onPostNotificationClickListener: OnPostNotificationClickListener) {
        mOnPostNotificationClickListener = onPostNotificationClickListener
    }

    fun setOnProfileNotificationClickListener(onProfileNotificationClickListener: OnProfileNotificationClickListener) {
        mOnProfileNotificationClickListener = onProfileNotificationClickListener
    }

    fun updateItem(notificationRecyclerViewItemList: ArrayList<Notification?>) {
        filteredList = notificationRecyclerViewItemList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_LOADING -> {
                LoadingViewHolder(ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            TYPE_FOLLOW_REQUEST -> {
                FollowRequestViewHolder(FollowRequestRecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            else -> {
                NotificationViewHolder(NotificationRecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NotificationViewHolder -> {
                val item = filteredList[position]!!
                holder.bind(item)
            }
            is FollowRequestViewHolder -> {
                val item = filteredList[position]!!
                holder.bind(item)
            }
            is LoadingViewHolder -> {

            }
        }
    }

    inner class LoadingViewHolder(private var binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    inner class NotificationViewHolder(private var binding: NotificationRecyclerViewItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.linearLayoutNotificationRecyclerViewItem.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && filteredList[position] != null) {
                    when(filteredList[position]!!.not_type) {
                        "follow" -> {
                            if (mOnProfileNotificationClickListener != null) {
                                mOnProfileNotificationClickListener.onItemClick(position)
                            }
                        }
                        else -> {
                            if (mOnPostNotificationClickListener != null) {
                                mOnPostNotificationClickListener.onItemClick(position)
                            }
                        }
                    }
                }
            }
        }

        @SuppressLint("NewApi")
        fun bind(notificationRecyclerViewItem: Notification) {
            binding.tvNotificationMessage.text = notificationRecyclerViewItem.not_message

            val dateTime : ZonedDateTime = OffsetDateTime.parse(notificationRecyclerViewItem.created_at).toZonedDateTime().plusHours(9)
            val formatter = DateTimeFormatter.ofPattern("yyyy年 MMM dd日 HH時 mm分 ", Locale.JAPANESE)
            binding.tvNotificationDate.text = dateTime.format(formatter)
        }
    }
    
    inner class FollowRequestViewHolder(private var binding: FollowRequestRecyclerViewItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.linearLayoutFollowRequestRecyclerViewItem.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && filteredList[position] != null && mOnProfileNotificationClickListener != null) {
                    mOnProfileNotificationClickListener.onItemClick(position)
                }
            }
        }

        @SuppressLint("NewApi")
        fun bind(notificationRecyclerViewItem: Notification) {
            binding.tvNotificationMessage.text = notificationRecyclerViewItem.not_message

            val dateTime : ZonedDateTime = OffsetDateTime.parse(notificationRecyclerViewItem.created_at).toZonedDateTime().plusHours(9)
            val formatter = DateTimeFormatter.ofPattern("yyyy年 MMM dd日 HH時 mm分 ", Locale.JAPANESE)
            binding.tvNotificationDate.text = dateTime.format(formatter)
        }
    }
}