package com.example.capstonandroid.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.R
import com.example.capstonandroid.network.dto.DataVo

class CustomAdapter(private val context : Context,
                    private val dataList : ArrayList<DataVo>):
    RecyclerView.Adapter<CustomAdapter.ItemViewHolder>(){
        inner class ItemViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
            private val userPhoto = itemView.findViewById<ImageView>(R.id.userImg)
            private val userName = itemView.findViewById<TextView>(R.id.userNameTxt)
            private val userPay = itemView.findViewById<TextView>(R.id.payTxt)
            private val userAddress = itemView.findViewById<TextView>(R.id.addressTxt)

            fun bind(dataVo : DataVo, context : Context){
                if (dataVo.photo != "") {
                    val resourceld =
                        context.resources.getIdentifier(dataVo.photo, "drawable",context.packageName)

                    if(resourceld > 0){
                        userPhoto.setImageResource(resourceld)
                    }
                    else {
                        userPhoto.setImageResource(R.mipmap.ic_launcher_round)
                    }
                }else {
                    userPhoto.setImageResource(R.mipmap.ic_launcher_round)
                }

                userName.text = dataVo.name
                userPay.text = dataVo.pay.toString()
                userAddress.text = dataVo.address
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.view_item_layout, parent,false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(dataList[position], context)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
    }