package com.example.capstonandroid

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// 자석처럼 짝 달라붙는 기능을 따로 만듬

fun RecyclerView.getScrollDistanceOfColumnClosestToLeft(): Int {
    val manager = layoutManager as LinearLayoutManager?
    val firstVisibleColumnViewHolder = findViewHolderForAdapterPosition(
        manager!!.findFirstVisibleItemPosition()
    ) ?: return 0
    val columnWidth = firstVisibleColumnViewHolder.itemView.measuredWidth
    val left = firstVisibleColumnViewHolder.itemView.left
    val absoluteLeft = Math.abs(left)
    return if (absoluteLeft <= columnWidth / 2) left else columnWidth - absoluteLeft
}

fun RecyclerView.setMagneticMove(nStart : Int = 0 ){
    addOnScrollListener(object:RecyclerView.OnScrollListener(){
        var oldMoveTo : Int = 0
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            val moveTo = getScrollDistanceOfColumnClosestToLeft() - nStart
            if(newState == RecyclerView.SCROLL_STATE_IDLE){
                // smoothScrollBy()를 사용했을 경우,
                // 양쪽 끝자리에서 움직이면 무한루핑이 됨.
                // 그래서 이전값과 비교함.
                if(moveTo !== oldMoveTo){
                    recyclerView.smoothScrollBy(moveTo, 0)
                    oldMoveTo = moveTo
                }
            }
        }
    })
}