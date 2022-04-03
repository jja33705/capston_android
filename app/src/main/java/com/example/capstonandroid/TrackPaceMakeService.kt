package com.example.capstonandroid

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.Room
import com.example.capstonandroid.activity.CompleteRecordActivity
import com.example.capstonandroid.activity.TrackPaceMakeActivity
import com.example.capstonandroid.db.AppDatabase
import com.example.capstonandroid.db.dao.GpsDataDao
import com.example.capstonandroid.db.dao.OpponentGpsDataDao
import com.example.capstonandroid.db.entity.GpsData
import com.example.capstonandroid.db.entity.OpponentGpsData
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import java.util.*

class TrackPaceMakeService : Service() {

    private lateinit var retrofit: Retrofit // 레트로핏 인스턴스
    private lateinit var supplementService: BackendApi // api

    private lateinit var mNotificationManager: NotificationManager // 상단바에 뜨는 노티피케이션 매니저

    private lateinit var mFusedLocationClient: FusedLocationProviderClient // 통합 위치 제공자 핸들

    private lateinit var mLocationCallback: LocationCallback // 위치 정보 업데이트 콜백

    // db 사용을 위한 data access object
    private lateinit var gpsDataDao: GpsDataDao
    private lateinit var opponentGpsDataDao: OpponentGpsDataDao

    private lateinit var beforeLocation: Location // 비교를 위한 이전 위치

    // 내 관련 정보
    private var sumAltitude: Double = 0.0 // 누적 상승 고도
    private var second: Int = 0 // 시간 (초)
    private var distance = 0.0 // 거리 (m)
    private var avgSpeed = 0.0 // 평균 속도

    private var opponentRecordEndSecond = 0

    private val timer = Timer() // 시간 업데이트를 위한 타이머

    companion object {
        var isStarted = false
        var trackName = ""
        var trackId = ""
        var exerciseKind = ""
        var matchType = ""
        var opponentPostId = 0
        var opponentGpsDataId = ""
        var opponentUserName = ""
        lateinit var mLocation: Location // 내 위치
        var myLocationIndexOnTrack = 0 // 내가 트랙 위에 어디쯤 존재하는지
        var mySumDistanceOnTrack = 0F // 내가 이동한 트랙위의 거리
        var myStaySecondOnTrack = 1 // 내가 트랙위에 한 지점에 머무른 시간
        lateinit var opponentLocation: Location // 상대 위치
        var opponentLocationIndexOnTrack = 0 // 상대가 트랙 위에 어디쯤 존재하는지
        var opponentSumDistanceOnTrack = 0F
        var opponentStaySecondOnTrack = 1

        private const val PREFIX = "com.example.capstonandroid.trackpacemakeservice"

        const val NOTIFICATION_CHANNEL_ID: String = PREFIX
        const val NOTIFICATION_CHANNEL_NAME: String = PREFIX
        const val NOTIFICATION_ID: Int = 1111

        const val ACTION_BROADCAST = "${PREFIX}.BROADCAST"

        // command
        const val START_RECORD = "${PREFIX}.STOP_RECORD"
        const val STOP_SERVICE = "${PREFIX}.STOP_SERVICE"
        const val START_PROCESS = "${PREFIX}.STOP_PROCESS"
        const val COMPLETE_RECORD = "${PREFIX}.COMPLETE_RECORD"

        // flag
        const val OPPONENT_START_LAT_LNG = "${PREFIX}.OPPONENT_START_LAT_LNG"
        const val BEFORE_START_LOCATION_UPDATE = "${PREFIX}.BEFORE_START_LOCATION_UPDATE"
        const val AFTER_START_UPDATE = "AFTER_START_UPDATE"
        const val RECORD_START_LAT_LNG = "${PREFIX}.RECORD_START_LAT_LNG"

        // intent keyword
        const val LAT_LNG = "${PREFIX}.LAT_LNG"
        const val SECOND = "${PREFIX}.SECOND"
        const val DISTANCE = "${PREFIX}.DISTANCE"
        const val AVG_SPEED = "${PREFIX}.AVG_SPEED"
        const val OPPONENT_LAT_LNG = "${PREFIX}.OPPONENT_LAT_LNG"
        const val SPEED = "${PREFIX}.SPEED"
        const val OPPONENT_SPEED = "${PREFIX}.OPPONENT_SPEED"

        // 1초에 갈 수 있다고 생각되는 최대 거리 30미터????
        const val MAX_DISTANCE = 30F
    }

    // 제일 처음 호출 (1회성으로 서비스가 이미 실행중이면 호출되지 않는다)
    override fun onCreate() {
        println("service: onCreate() 호출")

        initRetrofit()

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
                    intent.putExtra(LAT_LNG, LatLng(mLocation.latitude, mLocation.longitude))
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }
            }
        }

        // db 사용 설정
        val db = AppDatabase.getInstance(applicationContext)!!
        gpsDataDao = db.gpsDataDao()
        opponentGpsDataDao = db.opponentGpsDataDao()

        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager // 노티피케이션 매니저 초기롸

        createNotificationChannel() // 노티피케이션 채널 생성

        startForeground(NOTIFICATION_ID, getNotification()) // 포그라운드 서비스 시작
    }

    // 타이머 시작
    private fun startTimer() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                second ++

                // 고도가 만약 더 크면 누적 상승 고도 더해줌
                if (beforeLocation.altitude < mLocation.altitude) {
                    sumAltitude += mLocation.altitude - beforeLocation.altitude
                }

                // 거리 구해줌
                distance += beforeLocation.distanceTo(mLocation)

                //평균속도
                if (second > 0) {
                    avgSpeed = (distance / 1000) / (second.toDouble() / 3600)
                }

                beforeLocation = mLocation

                // 내 현재 상태 db에 저장
                gpsDataDao.insertGpsData(GpsData(second, mLocation.latitude, mLocation.longitude, mLocation.speed, distance, mLocation.altitude))


                // 상대 운동의 마지막 초까지만 가져오도록 조정
                var secondForGetOpponentGpsData = opponentRecordEndSecond
                if (second < secondForGetOpponentGpsData) {
                    secondForGetOpponentGpsData = second
                }

                // 상대 현재 상태 가져오기
                val opponentGpsData = opponentGpsDataDao.getOpponentGpsDataBySecond(secondForGetOpponentGpsData)
                opponentLocation.latitude = opponentGpsData.lat
                opponentLocation.longitude = opponentGpsData.lng

                // 상대방 속도
                var opponentSpeed = opponentGpsData.speed
                if (secondForGetOpponentGpsData == opponentRecordEndSecond) {
                    opponentSpeed = 0F
                }

                // 노티피케이션 업데이트
                mNotificationManager.notify(NOTIFICATION_ID, getNotification())

                // 업데이트된 것 브로드캐스트
                val intent = Intent(ACTION_BROADCAST)
                intent.putExtra("flag", AFTER_START_UPDATE)
                intent.putExtra(LAT_LNG, LatLng(mLocation.latitude, mLocation.longitude))
                intent.putExtra(OPPONENT_LAT_LNG, LatLng(opponentLocation.latitude, opponentLocation.longitude))
                intent.putExtra(SECOND, second)
                intent.putExtra(DISTANCE, distance)
                intent.putExtra(AVG_SPEED, avgSpeed)
                intent.putExtra(SPEED, mLocation.speed)
                intent.putExtra(OPPONENT_SPEED, opponentSpeed)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }

        }, 1000, 1000) // 1초 후 시작, 1초 간격
    }

    // notification 만들기
    private fun getNotification(): Notification {

        // 알람 누르면 액티비티 시작하게 하는 pendingIntent
        val activityIntent = Intent(applicationContext, TrackPaceMakeActivity::class.java)
        activityIntent.putExtra("exerciseKind", exerciseKind)
        activityIntent.putExtra("matchType", matchType)
        activityIntent.putExtra("trackId", trackId)
        activityIntent.putExtra("opponentGpsDataId", opponentGpsDataId)
        activityIntent.putExtra("opponentPostId", opponentPostId)

        val activityPendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentText("$trackName   ${Utils.timeToText(second)}    ${Utils.distanceToText(distance)}km")
            .setContentTitle("페이스메이커")
            .setOngoing(true) //종료 못하게 막음
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(activityPendingIntent) // 알람을 눌렀을 때 실행할 작업
            .setWhen(System.currentTimeMillis())

        return builder.build()
    }

    // 레트로핏 초기화
    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java)
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
            START_PROCESS -> { // 액티비티 실행되고 프로세스 시작
                trackId = intent.getStringExtra("trackId")!!
                trackName = intent.getStringExtra("trackName")!!
                exerciseKind = intent.getStringExtra("exerciseKind")!!
                matchType = intent.getStringExtra("matchType")!!
                opponentPostId = intent.getIntExtra("opponentPostId", 0)
                opponentGpsDataId = intent.getStringExtra("opponentGpsDataId")!!
                mNotificationManager.notify(NOTIFICATION_ID, getNotification())

                // 상대 gps data 가져와서 db에 넣고 액티비티로 보냄
                CoroutineScope(Dispatchers.Main).launch {
                    // 상대 gps 데이터 가져옴
                    val token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!
                    val gpsDataResponse = supplementService.getGpsData(token, opponentGpsDataId)
                    if (gpsDataResponse.isSuccessful) {
                        val opponentGpsData = gpsDataResponse.body()!!.gpsData
                        opponentRecordEndSecond = opponentGpsData.totalTime
                        opponentUserName = opponentGpsData.user.name

                        launch(Dispatchers.IO) {
                            // 모두 db에 저장
                            opponentGpsDataDao.deleteAllOpponentGpsData()
                            opponentGpsData.time.map { second ->
                                opponentGpsDataDao.insertOpponentGpsData(OpponentGpsData(second, opponentGpsData.gps.coordinates[second][1], opponentGpsData.gps.coordinates[second][0], opponentGpsData.speed[second].toFloat(), opponentGpsData.distance[second], opponentGpsData.altitude[second]))
                            }

                            // 상대 시작 위치 가져오기
                            opponentLocation = Location("opponentLocation")
                            val opponentGpsData = opponentGpsDataDao.getOpponentGpsDataBySecond(second)
                            opponentLocation.latitude = opponentGpsData.lat
                            opponentLocation.longitude = opponentGpsData.lng

                            println("다 넣었을까: ${opponentGpsDataDao.getAllOpponentGpsData()}")
                        }.join()

                        // 상대 시작위치 보냄
                        val intent = Intent(ACTION_BROADCAST)
                        intent.putExtra("flag", OPPONENT_START_LAT_LNG)
                        intent.putExtra(OPPONENT_LAT_LNG, LatLng(opponentLocation.latitude, opponentLocation.longitude))
                        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                    }
                    createLocationRequest() // 위치 업데이트 시작
                }
            }
            START_RECORD -> { // 기록 시작
                CoroutineScope(Dispatchers.Main).launch {
                    isStarted = true

                    beforeLocation = mLocation

                    launch(Dispatchers.IO) {
                        // 시작위치 db에 저장
                        gpsDataDao.deleteAllGpsData()
                        gpsDataDao.insertGpsData(GpsData(second, mLocation.latitude, mLocation.longitude, mLocation.speed, distance, mLocation.altitude))
                    }.join()

                    // 시작 위치 보냄
                    val intent = Intent(ACTION_BROADCAST)
                    intent.putExtra("flag", RECORD_START_LAT_LNG)
                    intent.putExtra(LAT_LNG, LatLng(mLocation.latitude, mLocation.longitude))
                    intent.putExtra(OPPONENT_LAT_LNG, LatLng(opponentLocation.latitude, opponentLocation.longitude))
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                    // 타이머 시작
                    startTimer()
                }
            }
            COMPLETE_RECORD -> { // 기록 끝
                val intent = Intent(this@TrackPaceMakeService, CompleteRecordActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("avgSpeed", avgSpeed)
                intent.putExtra("kcal", 30.0)
                intent.putExtra("sumAltitude", sumAltitude)
                intent.putExtra("second", second)
                intent.putExtra("matchType", matchType)
                intent.putExtra("trackId", trackId)
                intent.putExtra("exerciseKind", exerciseKind)
                intent.putExtra("opponentPostId", opponentPostId)

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

    private fun stopService() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback) // 위치 업데이트 제거
        timer.cancel() // 타이머 제거

        isStarted = false

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