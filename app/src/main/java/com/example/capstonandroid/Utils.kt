package com.example.capstonandroid

import android.content.Context
import android.location.Location
import androidx.preference.PreferenceManager
import java.text.DateFormat
import java.util.*

class Utils {
    companion object {
        const val KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates"

        // location update 중인지 반환
        fun requestingLocationUpdates(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false)
        }

        // location update 값 수정
        fun setRequestingLocationUpdates(context: Context, requestingLocationUpdates: Boolean) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply()
        }

        // location 받아서 위도 경도로 반환
        fun getLocationText(location: Location?): String {
            return if(location == null) {
                "Unknown location"
            } else {
                "(${location.latitude}, ${location.longitude})"
            }
        }

        // 없어두 됨
        fun getLocationTitle(context: Context): String {
            return "abcd"
//            return context.getString(R.string.location_updated,
//                DateFormat.getDateTimeInstance().format(Date()))
        }

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