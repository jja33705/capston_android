package com.example.capstonandroid

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.capstonandroid.activity.RecordActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task

class RecordService : Service() {

    private lateinit var mNotificationManager: NotificationManager // 상단바에 뜨는 노티피케이션 매니저

    private val mBinder: IBinder = MBinder() // 바인더

    var timeValue = 0

    companion object {
        const val NOTIFICATION_CHANNEL_ID: String = "com.example.capstonandroid"
        const val NOTIFICATION_CHANNEL_NAME: String = "test"
        const val NOTIFICATION_ID: Int = 1234
    }

    // 노티피케이션 업데이트
    fun updateNotification() {
        mNotificationManager.notify(NOTIFICATION_ID, getNotification())
    }


    // 제일 처음 호출 (1회성으로 서비스가 이미 실행중이면 호출되지 않는다)
    override fun onCreate() {
        println("service: onCreate() 호출")

        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager // 노티피케이션 매니저 초기롸

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, getNotification())
    }

    // notification 만들기
    private fun getNotification(): Notification {

        // 알람 누르면 액티비티 시작하게 하는 pendingIntent
        val activityIntent = Intent(applicationContext, RecordActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentText(Utils.timeToText(timeValue))
            .setContentTitle("페이스메이커")
            .setOngoing(true) //종료 못하게 막음
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(activityPendingIntent) // 알람을 눌렀을 때 실행할 작업
            .setWhen(System.currentTimeMillis())

        return builder.build()
    }

    // notification 등록을 위한 채널 생성
    @SuppressLint("NewApi")
    private fun createNotificationChannel() {
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW) // IMPORTANCE_DEFAULT: 알림에 소리만 사용한다.
        mNotificationManager.createNotificationChannel(channel)
    }

    // 바인드됐을 때 호출
    override fun onBind(intent: Intent?): IBinder? {
        println("service: onBind() 호출")
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        println("service: onUnBind() 호출")
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        println("service: onReBind() 호출")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("service: onStartCommand 호출, action: ${intent?.action}")
        return START_NOT_STICKY // 서비스 중단하면 재생성하지 않는다.
    }

    override fun onDestroy() {
        println("service: onDestroy() 호출")
        super.onDestroy()
    }

    // 바인더 클래스
    inner class MBinder: Binder() {
        fun getService(): RecordService? {
            return this@RecordService
        }
    }
}