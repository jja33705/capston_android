package com.example.capstonandroid.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.R
import kotlin.random.Random


// 추후 스토리?같은 기능ㅋㅋ
sealed class SimpleData
data class Box(
    var color        : Int,
    var alpha        : Float
) : SimpleData()

class MagneticAdapter(val items : List<SimpleData>, val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mItems : MutableList <SimpleData> = items.toMutableList()

    override fun getItemCount(): Int {
        return mItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType){
            TYPE_ONE   -> {boxViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false))}
            else -> {boxViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false))}
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder.itemViewType){
            TYPE_ONE -> { (holder as boxViewHolder).apply {
                var item = mItems.get(position) as Box
                bind(context,  item)
            }}
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (mItems.get(position)){
            is Box -> {
                TYPE_ONE
            }
        }
    }

    companion object {
        private const val TYPE_ONE   = 0
    }
}

class boxViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val box = view.findViewById<View>(R.id.colorBox)
    val dimmed = view.findViewById<View>(R.id.dimmed)
    fun bind(context : Context, item : Box){
        box.setBackgroundColor(item.color)

        dimmed.setBackgroundColor(Color.BLACK)
        dimmed.alpha = item.alpha
    }
}