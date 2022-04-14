package com.example.capstonandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.R
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.UserData
import retrofit2.Retrofit
// 여기가 Me

class RecyclerUserAdapter(
    private val items: ArrayList<UserData>,
    private val onClick: (UserData) -> Unit) : RecyclerView.Adapter<RecyclerUserAdapter.ViewHolder>() {

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerUserAdapter.ViewHolder, position: Int) {
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
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return RecyclerUserAdapter.ViewHolder(inflatedView)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        fun bind(listener: View.OnClickListener, item: UserData) {
            view.findViewById<TextView>(R.id.txtUser_title).text = item.name
            view.findViewById<TextView>(R.id.txtUser_name).text = item.title
            view.findViewById<TextView>(R.id.txtUser_created_id).text = item.created_id
            view.findViewById<TextView>(R.id.page).text = item.page.toString()
            view.setOnClickListener(listener)
        }
    }
}