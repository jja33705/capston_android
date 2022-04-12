package com.example.capstonandroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.R
import kotlinx.android.synthetic.main.idol_list_item.view.*

class ViewPagerAdapter(idolList: ArrayList<Int>) : RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder>() {
    var item = idolList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PagerViewHolder((parent))

    override fun getItemCount(): Int = item.size

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        if(position == 1) {
            holder.idol.setText("da")
        }
        else {
            holder.idol.setText("ㅁㄴㅇㅁㄴㅇㅁㄴ")
        }
        }

    inner class PagerViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder
        (LayoutInflater.from(parent.context).inflate(R.layout.idol_list_item, parent, false)){

        val idol = itemView.content
    }
}