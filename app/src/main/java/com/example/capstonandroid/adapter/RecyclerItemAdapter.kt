package com.example.capstonandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.R
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.UserData
import kotlinx.android.synthetic.main.item_view.view.*
import retrofit2.Retrofit

class RecyclerUserAdapter(private val items: ArrayList<UserData>) : RecyclerView.Adapter<RecyclerUserAdapter.ViewHolder>() {


    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerUserAdapter.ViewHolder, position: Int) {




        val item = items[position]
        val listener = View.OnClickListener { it ->
            Toast.makeText(it.context, "Clicked -> ID : ${item.id}, Name : ${item.name}", Toast.LENGTH_SHORT).show()
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

    // 각 항목에 필요한 기능을 구현
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        fun bind(listener: View.OnClickListener, item: UserData) {
            view.txtUser_id.text = item.id
            view.txtUser_name.text = item.name
            view.setOnClickListener(listener)
        }
    }


}