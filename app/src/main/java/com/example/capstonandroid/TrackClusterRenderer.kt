package com.example.capstonandroid

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.capstonandroid.activity.SelectTrackActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer


class TrackClusterRenderer(context: Context, map: GoogleMap, clusterManager: ClusterManager<SelectTrackActivity.TrackItem>) : DefaultClusterRenderer<SelectTrackActivity.TrackItem>(context, map, clusterManager) {

    private var trackMarker: View = LayoutInflater.from(context).inflate(R.layout.track_and_name_marker, null)!! // 커스텀 마커 뷰
    private var trackMarkerTextView: TextView = trackMarker.findViewById(R.id.tv_marker) as TextView // 커스텀 마커 텍스트 뷰

    // 비트맵 이미지 만드는 함수
    private fun createBitmapFromView(): Bitmap {
        trackMarker.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        trackMarker.layout(0, 0, trackMarker.measuredWidth, trackMarker.measuredHeight)

        val bitmap = Bitmap.createBitmap(trackMarker.measuredWidth,
            trackMarker.measuredHeight,
            Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)

        trackMarker.background?.draw(canvas)
        trackMarker.draw(canvas)

        return bitmap
    }

    // 마커 아이콘 바꿔 줌
    override fun onBeforeClusterItemRendered(item: SelectTrackActivity.TrackItem, markerOptions: MarkerOptions) {
        trackMarkerTextView.text = item.title
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView()))
            .anchor(0.08F, 1F)
    }
}