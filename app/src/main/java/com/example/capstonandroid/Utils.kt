package com.example.capstonandroid

import android.content.Context
import android.location.Location
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
    }
}