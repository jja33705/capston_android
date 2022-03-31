package com.example.capstonandroid

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.view.View
import java.text.DateFormat
import java.util.*

class Utils {
    companion object {
        fun timeToText(time: Int): String {
            return if (time == 0) {
                "00:00:00"
            } else {
                val h = time / 3600
                val m = time % 3600 / 60
                val s = time % 60
                "%1$02d:%2$02d:%3$02d".format(h, m, s)
            }
        }

        fun distanceToText(distance: Double): String {
            return "%.2f".format(distance / 1000)
        }

        fun avgSpeedToText(avgSpeed: Double): String {
            return "%.2f".format(avgSpeed)
        }

        fun speedToText(speed: Float): String {
            return "%.2fkm/h".format(speed)
        }

        // 비트맵 이미지 만드는 함수
        fun createBitmapFromView(marker: View): Bitmap {
            marker.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            marker.layout(0, 0, marker.measuredWidth, marker.measuredHeight)

            val bitmap = Bitmap.createBitmap(marker.measuredWidth,
                marker.measuredHeight,
                Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)

            marker.background?.draw(canvas)
            marker.draw(canvas)

            return bitmap
        }
    }
}