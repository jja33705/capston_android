package com.example.capstonandroid

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.capstonandroid.activity.CompleteRecordActivity
import com.example.capstonandroid.activity.RecordActivity
import com.example.capstonandroid.db.AppDatabase
import com.example.capstonandroid.db.dao.GpsDataDao
import com.example.capstonandroid.db.entity.GpsData
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class RecordService : Service() {

    private lateinit var mNotificationManager: NotificationManager // 상단바에 뜨는 노티피케이션 매니저

    private lateinit var mFusedLocationClient: FusedLocationProviderClient // 통합 위치 제공자 핸들

    private lateinit var mLocationCallback: LocationCallback // 위치 정보 업데이트 콜백

    private lateinit var gpsDataDao: GpsDataDao // db 사용을 위한 data access object

    private lateinit var beforeLocation: Location // 비교를 위한 이전 위치

    private val timer = Timer() // 시간 업데이트를 위한 타이머
    private var weight = 0 // 칼로리 계산을 위한 몸무게

    private var sumAltitude: Double = 0.0 // 누적 상승 고도
    private var second: Int = 0 // 시간 (초)
    private var distance = 0.0 // 거리 (m)
    private var avgSpeed = 0.0 // 평균 속도
    private var calorie = 0.0 // 칼로리

    companion object {
        var isStarted = false // 시작 했는지
        var exerciseKind = "" // 운동 종류
        var mLocation: Location? = null // 음... 액티비티에서 이전 위치 바로 초기화해야 하니까 여기에 놓자

        private const val PREFIX = "com.example.capstonandroid.recordservice"

        const val NOTIFICATION_CHANNEL_ID: String = PREFIX
        const val NOTIFICATION_CHANNEL_NAME: String = PREFIX
        const val NOTIFICATION_ID: Int = 1111

        const val ACTION_BROADCAST = "$PREFIX.BROADCAST"

        // command
        const val START_RECORD = "$PREFIX.STOP_RECORD"
        const val STOP_SERVICE = "$PREFIX.STOP_SERVICE"
        const val START_PROCESS = "$PREFIX.STOP_PROCESS"
        const val COMPLETE_RECORD = "$PREFIX.COMPLETE_RECORD"

        // flag
        const val BEFORE_START_LOCATION_UPDATE = "$PREFIX.BEFORE_START_LOCATION_UPDATE"
        const val AFTER_START_UPDATE = "AFTER_START_UPDATE"
        const val RECORD_START_LAT_LNG = "$PREFIX.RECORD_START_LAT_LNG"

        // intent keyword
        const val LAT_LNG = "$PREFIX.LAT_LNG"
        const val SECOND = "$PREFIX.SECOND"
        const val DISTANCE = "$PREFIX.DISTANCE"
        const val AVG_SPEED = "$PREFIX.AVG_SPEED"
        const val LOCATION_CHANGED = "$PREFIX.LOCATION_CHANGED"
        const val CALORIE = "$PREFIX.CALORIE"
    }

    // 제일 처음 호출 (1회성으로 서비스가 이미 실행중이면 호출되지 않는다)
    override fun onCreate() {
        println("service: onCreate() 호출")

        weight = getSharedPreferences("other", MODE_PRIVATE).getInt("WEIGHT", 0)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this) // 통합 위치 제공자 초기화

        // 위치 요청 응답 왔을 때 콜백
        mLocationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                mLocation = locationResult.lastLocation

                // 기록 시작하기 전에 는 위치 계속 갱신해 줌
                if (!isStarted) {

                    // 로컬 프로드캐스트를 통해 위치를 보낸다.
                    val intent = Intent(ACTION_BROADCAST)
                    intent.putExtra("flag", BEFORE_START_LOCATION_UPDATE)
                    intent.putExtra(LAT_LNG, LatLng(mLocation!!.latitude, mLocation!!.longitude))
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }
            }
        }

        // db 사용 설정
        val db = AppDatabase.getInstance(applicationContext)!!
        gpsDataDao = db.gpsDataDao()

        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager // 노티피케이션 매니저 초기롸

        createNotificationChannel() // 노티피케이션 채널 생성

        startForeground(NOTIFICATION_ID, getNotification()) // 포그라운드 서비스 시작

        createLocationRequest() // 위치 업데이트 시작
    }

    // 타이머 시작
    private fun startTimer() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                second ++

                val locationChanged = (mLocation!!.latitude != beforeLocation.latitude) || (mLocation!!.longitude != beforeLocation.longitude)

                // 위치가 다르면 수행할 작업들
                if (locationChanged) {
                    // 고도가 만약 더 크면 누적 상승 고도 더해줌
                    if (beforeLocation.altitude < mLocation!!.altitude) {
                        sumAltitude += mLocation!!.altitude - beforeLocation.altitude
                    }

                    // 거리 구해줌
                    distance += beforeLocation.distanceTo(mLocation)
                    println("거리: $distance")

                    beforeLocation = mLocation!!

                    // db에 저장
                    gpsDataDao.insertGpsData(GpsData(second, mLocation!!.latitude, mLocation!!.longitude, mLocation!!.speed, distance, mLocation!!.altitude))
                }

                // 평균속도
                if (second > 0) {
                    avgSpeed = (distance / 1000) / (second.toDouble() / 3600)
                    println(avgSpeed)
                }

                // 칼로리
                when (exerciseKind) {
                    "R" -> {
                        calorie += 0.15
                    }
                    "B" -> {
                        calorie += 0.16
                    }
                }

                // 노티피케이션 업데이트
                mNotificationManager.notify(NOTIFICATION_ID, getNotification())

                // 업데이트된 것 브로드캐스트
                val intent = Intent(ACTION_BROADCAST)
                intent.putExtra("flag", AFTER_START_UPDATE)
                intent.putExtra(LAT_LNG, LatLng(mLocation!!.latitude, mLocation!!.longitude))
                intent.putExtra(SECOND, second)
                intent.putExtra(DISTANCE, distance)
                intent.putExtra(AVG_SPEED, avgSpeed)
                intent.putExtra(LOCATION_CHANGED, locationChanged)
                intent.putExtra(CALORIE, calorie)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        }, 1000, 1000) // 1초 후 시작, 1초 간격
    }

    // notification 만들기
    private fun getNotification(): Notification {

        // 알람 누르면 액티비티 시작하게 하는 pendingIntent
        val activityIntent = Intent(applicationContext, RecordActivity::class.java)
        activityIntent.putExtra("exerciseKind", exerciseKind)
        val activityPendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentText("${Utils.timeToText(second)}    ${Utils.distanceToText(distance)}km")
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

    // 커맨드 시작
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("service: onStartCommand 호출, action: ${intent?.action}")

        when(intent?.action) {
            START_PROCESS -> { // 서비스 처음 실행할 때
                exerciseKind = intent.getStringExtra("exerciseKind")!!
                println("RecordService: 서비스 시작에서 운동 종류 받음 $exerciseKind")
            }
            START_RECORD -> { // 기록 시작
                CoroutineScope(Dispatchers.Main).launch {
                    isStarted = true

                    beforeLocation = mLocation!!

                    // 시작위치 db에 저장
                    launch(Dispatchers.IO) {
                        gpsDataDao.deleteAllGpsData()
                        gpsDataDao.insertGpsData(GpsData(second, mLocation!!.latitude, mLocation!!.longitude, mLocation!!.speed, distance, mLocation!!.altitude))
                    }.join()

                    // 시작 위치 보냄
                    val intent = Intent(ACTION_BROADCAST)
                    intent.putExtra("flag", RECORD_START_LAT_LNG)
                    intent.putExtra(LAT_LNG, LatLng(mLocation!!.latitude, mLocation!!.longitude))
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                    // 타이머 시작
                    startTimer()
                }
            }
            COMPLETE_RECORD -> { // 기록 끝
                val intent = Intent(this@RecordService, CompleteRecordActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("avgSpeed", avgSpeed)
                intent.putExtra("kcal", calorie)
                intent.putExtra("sumAltitude", sumAltitude)
                intent.putExtra("second", second)
                intent.putExtra("exerciseKind", exerciseKind)
                intent.putExtra("matchType", "자유")
                intent.putExtra("distance", distance)

                startActivity(intent)

                stopService()
            }
            STOP_SERVICE -> { // 서비스 종료
                stopService()
            }
        }

        return START_NOT_STICKY // 서비스 중단하면 재생성하지 않는다.
    }

    @SuppressLint("MissingPermission")
    fun createLocationRequest() {
        mFusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            // 내 마지막 위치 못 가져왔을 때
            if (location == null) {
                println("마지막 위치 못가져옴")
            } else {
                println("마지막 위치 잘 가져옴, 위도: ${location.latitude}, 경도: ${location.longitude}")
                mLocation = location
            }
        }

        // 위치 요청 생성
        val locationRequest = LocationRequest.create()?.apply {
            interval = 2000 // 간격
            fastestInterval = 1000 // 최대 간격 ( 다른 곳에서 위치정보 수집해도, 여기서도 받아짐. 그떄의 최대 간격 )
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // 위치 요청을 위치 서비스 api 에 연결하고 위치 설정 잘 됐는지 확인
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            println("location client 설정 성공")
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper()) // 위치 업데이트 요청
        }
        task.addOnFailureListener {
            println("location client 설정 실패")
        }
    }

    // 리소스 해제하고 서비스 종료
    private fun stopService() {
        timer.cancel() // 타이머 제거
        mFusedLocationClient.removeLocationUpdates(mLocationCallback) // 위치 업데이트 제거

        // companion object 리셋
        isStarted = false
        exerciseKind = ""
        mLocation = null

        stopForeground(true)
        stopSelf()
    }


    override fun onDestroy() {
        println("service: onDestroy() 호출")
        super.onDestroy()
    }

    // 바인드됐을 때 호출
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}