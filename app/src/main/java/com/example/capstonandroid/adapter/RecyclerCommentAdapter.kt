package com.example.capstonandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.R
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.Comment
import com.example.capstonandroid.network.dto.CommentData
import com.example.capstonandroid.network.dto.UserData
import kotlinx.android.synthetic.main.comment_item_view.view.*
import kotlinx.android.synthetic.main.item_view.view.*
import retrofit2.Retrofit

class RecyclerCommentAdapter(
    private val items: ArrayList<CommentData>,
    private val onClick: (CommentData) -> Unit) : RecyclerView.Adapter<RecyclerCommentAdapter.ViewHolder>() {

    override fun getItemCount(): Int = items.size

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
        fun bind(listener: View.OnClickListener, item: CommentData) {
            view.user_name.text = item.username
            view.user_content.text = item.content
            view.user_created_at.text = item.created_at
            view.user_updated_at.text = item.updated_at
            view.setOnClickListener(listener)
        }
    }
}