package com.example.capstonandroid

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class DistanceItemDecorator(private val divValue: Int, private val divColor: Int = Color.TRANSPARENT) : RecyclerView.ItemDecoration() {


    private val paint = Paint()

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        //super.onDraw(c, parent, state)
        dividerColor(c, parent, color = divColor)
    }


    private fun dividerColor(c: Canvas, parent: RecyclerView, color: Int) {
        paint.color = color

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val param = child.layoutParams as RecyclerView.LayoutParams

            val dividerTop = child.bottom
            val dividerBottom = dividerTop + divValue + divValue  // 위, 아래 간격 값을 주기 위해 두번

            c.drawRect(
                child.left.toFloat(),
                dividerTop.toFloat(),
                child.right.toFloat(),
                dividerBottom.toFloat(),
                paint
            )
        }
    }


    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        outRect.top = divValue
        outRect.left = divValue
        outRect.bottom = divValue
        outRect.right = divValue
    }

}