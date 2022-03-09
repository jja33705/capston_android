package com.example.capstonandroid

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.capstonandroid.activity.RecordActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class TrackRecordService : Service() {

    private lateinit var mNotificationManager: NotificationManager // 상단바에 뜨는 노티피케이션 매니저

    private lateinit var updateTimer: Runnable // 타이머 업데이트

    private lateinit var mFusedLocationClient: FusedLocationProviderClient // 통합 위치 제공자 핸들

    private lateinit var mLocationCallback: LocationCallback // 위치 정보 업데이트 콜백

    private lateinit var mLocation: Location // 내 위치

    private lateinit var beforeLocation: Location // 비교를 위한 이전 위치

    private val locationList = ArrayList<Location>() // 위치 리스트

    private val mHandler= Handler(Looper.getMainLooper()) // 타이머를 위한 핸들러 (메인루퍼와 연결함)

    private var sumAltitude: Double = 0.0 // 누적 상승 고도

    private var second: Int = 0 // 시간 (초)

    private var distance = 0.0 // 거리 (m)

    private var avgSpeed = 0.0 // 평균 속도

    private var isStarted = false // 시작했는지

    // 제일 처음 호출 (1회성으로 서비스가 이미 실행중이면 호출되지 않는다)
    override fun onCreate() {
        println("service: onCreate() 호출")

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this) // 통합 위치 제공자 초기화

        // 위치 요청 응답 왔을 때 콜백
        mLocationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                mLocation = locationResult.lastLocation

                // 기록 시작하기 전에 는 위치 계속 갱신해 줌
                if (!isStarted) {

                    // 로컬 프로드캐스트를 통해 위치를 보낸다.
                    val intent = Intent(RecordService.ACTION_BROADCAST)
                    intent.putExtra("flag", RecordService.BEFORE_START_LOCATION_UPDATE)
                    intent.putExtra(RecordService.LOCATION, mLocation)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }
            }
        }
    }

    // notification 만들기
    private fun getNotification(): Notification {

        // 알람 누르면 액티비티 시작하게 하는 pendingIntent
        val activityIntent = Intent(applicationContext, RecordActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, RecordService.NOTIFICATION_CHANNEL_ID)
            .setContentText("${Utils.timeToText(second)}    ${Utils.distanceToText(distance)}km")
            .setContentTitle("페이스메이커")
            .setOngoing(true) //종료 못하게 막음
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(activityPendingIntent) // 알람을 눌렀을 때 실행할 작업
            .setWhen(System.currentTimeMillis())

        return builder.build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}