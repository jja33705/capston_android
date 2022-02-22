package com.example.capstonandroid

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.capstonandroid.activity.RecordActivity
import com.google.android.gms.location.*

class RecordService_example : Service() {

    private val EXTRA_STARTED_FROM_NOTIFICATION = "RecordService.started_from_notification"
    private val CHANNEL_ID = "channel_01"
    private val NOTIFICATION_ID = 12345678

    private var mChangingConfiguration = false
    private lateinit var mNotificationManager: NotificationManager // 상단바에 뜨는 노티피케이션 매니저
    private val mBinder: IBinder = LocalBinder() // 바인더
    private var mFusedLocationClient: FusedLocationProviderClient? = null // 통합 위치 제공자
    private var mLocationRequest: LocationRequest? = null // 위치정보 요청자
    private lateinit var mLocationCallback: LocationCallback // 위치정보 업데이트 됐을 때 콜백
    private var mLocation: Location? = null // 내 위치

    companion object {
        const val EXTRA_LOCATION = "RecordService.location"
        const val ACTION_BROADCAST = "RecordService.broadcast"
    }

    // 제일 처음 호출 (1회성으로 서비스가 이미 실행중이면 호출되지 않는다)
    @SuppressLint("NewApi")
    override fun onCreate() {
        //super.onCreate()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this) // 통합 위치 제공자 초기화
        mLocationCallback = object : LocationCallback() { // 위치정보 업데이트 됐을 때 콜백 초기화
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult.lastLocation)
            }
        }
        createLocationRequest()
        getLastLocation()

        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // 노티피케이션 채널 생성
        val name: String = getString(R.string.app_name)
        val mChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT) // 채널 아이디, 이름, 중요도
        mNotificationManager.createNotificationChannel(mChannel)
    }

    // 서비스를 시작하도록 요청(이 메서드가 실행되면 서비스가 시작된다.)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("onStartCommand() 호출")

        // notification 으로 시작됐을 떄
        val startedFromNotification = intent!!.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION, false)
        if (startedFromNotification) {
            removeLocationUpdates()
            stopSelf() // 서비스 중단
        }
        return START_NOT_STICKY // 서비스 중단하면 재생성하지 않는다.
        //super.onStart(intent, startId)
    }

    fun onNewLocation(location: Location) {
        println("new Location$location")
        mLocation = location

        // 로컬 프로드캐스트를 통해 위치를 보낸다.
        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_LOCATION, location)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

        if(serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification())
        }
    }

    // 위치 요청 생성
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create()?.apply {
            interval = 2000 // 간격
            fastestInterval = 1000 // 최대 간격 ( 다른 앱에서 위치정보 수집해도, 여기서도 받아지는 듯 함? )
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    fun removeLocationUpdates() {
        println("removeLocationUpdates() 호출")
        try {
            // 위치 업데이트 제거
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
            Utils.setRequestingLocationUpdates(this, false)

            // 서비스 종료
            stopSelf()
        } catch (unlikely: SecurityException) {
            Utils.setRequestingLocationUpdates(this, true)
        }
    }

    private fun getNotification(): Notification {
        val intent:Intent = Intent(this, RecordService_example::class.java)
        val text = Utils.getLocationText(mLocation)
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)

        // pendingIntent: 특정시점에 실행되는 intent
        val servicePendingIntent: PendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val activityPendingIntent = PendingIntent.getActivity(this, 0, Intent(this, RecordActivity::class.java), 0)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .addAction(R.drawable.ic_launcher_background, "액티비티로 이동", activityPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "종료", servicePendingIntent)
            .setContentText(text)
            .setContentTitle(Utils.getLocationTitle(this))
            .setOngoing(true) //종료 못하게 막음
            .setPriority(Notification.PRIORITY_MAX)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker(text)
            .setWhen(System.currentTimeMillis())

        builder.setChannelId(CHANNEL_ID)
        return builder.build()
    }

    // 화면방향, 휴대폰 Locale등이 변경되어 재시작되면 호출
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mChangingConfiguration = true
    }


    // 다른 구성요소와 바인딩하면 호출
    override fun onBind(intent: Intent): IBinder {
        println("onBind() 호출")
        stopForeground(true)
        mChangingConfiguration = false
        return mBinder
        TODO("Return the communication channel to the service.")
    }

    // 다시 바인딩되면 호출
    override fun onRebind(intent: Intent?) {
        println("onRebind() 호출")
        stopForeground(true)
        mChangingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        println("onUnBind() 호출")

        // 구성 변경 하지 않았고, 위치 업데이트 중일 때 포그라운드 서비스 시작
        if(!mChangingConfiguration && Utils.requestingLocationUpdates(this)) {
            println("Start foreground service")
            startForeground(NOTIFICATION_ID, getNotification())
        }
        return true
    }

    override fun onDestroy() {
        //super.onDestroy()
    }

    // 마지막 위치 가져옴
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        mFusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
            // 내 마지막 위치 못 가져왔을 때
            if (location == null) {
                println("마지막 위치 못가져옴")
            } else {
                println("마지막 위치 잘 가져옴")
                mLocation = location
            }
        }
    }

    // 서비스가 포그라운드에서 동작 중인지 확인
    private fun serviceIsRunningInForeground(context: Context):Boolean {
        val manager = context.getSystemService(
            ACTIVITY_SERVICE) as ActivityManager

        for (service in manager.getRunningServices(Int.MAX_VALUE)) { // 현재 시스템에서 동작 중인 모든 서비스 목록을 얻어 반복
            println("Service is ${service.service.className}")
            if (javaClass.name == service.service.className) { // 이 서비스가 포그라운드 상태면 true 반환
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }

    // 바인더 클래스
    inner class LocalBinder: Binder() {
        fun getService(): RecordService_example? {
            return this@RecordService_example
        }
    }
}