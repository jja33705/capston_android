package com.example.capstonandroid

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.maps.GoogleMap
import java.io.FileOutputStream

class MySnapshotReadyCallback(private val mContext: Context) : GoogleMap.SnapshotReadyCallback {
    override fun onSnapshotReady(snapshot: Bitmap?) {
//      앱 내부 cache 저장소: /data/user/0/com.example.capstonandroid/cache

        // 이미 있는 이미지는 삭제
        val file = mContext.cacheDir
        val fileList = file.listFiles()
        for (file in fileList) {
            if (file.name == "map.png") {
                file.delete()
            }
        }

        // 이미지 저장
        val fileOutputStream = FileOutputStream("${mContext.cacheDir}/map.png")
        snapshot?.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.close()
        println("이미지 저장 끝남")
    }
}