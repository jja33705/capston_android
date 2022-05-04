package com.example.capstonandroid.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.capstonandroid.*
import com.example.capstonandroid.databinding.ActivityTrackPaceMakeBinding
import com.example.capstonandroid.db.AppDatabase
import com.example.capstonandroid.db.dao.GpsDataDao
import com.example.capstonandroid.db.dao.OpponentGpsDataDao
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.Track
import com.example.capstonandroid.service.TrackPaceMakeService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.checkpoint_dialog.*
import kotlinx.android.synthetic.main.complete_record_dialog.*
import kotlinx.coroutines.*
import retrofit2.Retrofit
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList

class TrackPaceMakeActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.SnapshotReadyCallback {

    private var _binding: ActivityTrackPaceMakeBinding? = null
    private val binding get() = _binding!!

    private lateinit var exerciseKind: String
    private lateinit var matchType: String
    private lateinit var trackId: String
    private lateinit var opponentGpsDataId: String // 상대방의 gps data id
    private var opponentPostId = 0 // 상대방 post id
    private var opponentAvgSpeed = 0.0
    private var opponentTime = 0

    private lateinit var retrofit: Retrofit // 레트로핏 인스턴스
    private lateinit var supplementService: BackendApi // api

    private lateinit var mGoogleMap: GoogleMap

    private lateinit var textToSpeech: TextToSpeech // tts
    private var textToSpeechInitialized = false // tts 초기화 됐는지

    private lateinit var checkpointMarkerList: ArrayList<Marker>
    private lateinit var checkpointDialog: Dialog

    // 완료시 비교화면 dialog
    private lateinit var completeRecordDialog: Dialog

    // db dao 핸들
    private lateinit var gpsDataDao: GpsDataDao
    private lateinit var opponentGpsDataDao: OpponentGpsDataDao

    private lateinit var track: Track

    // 트랙 시작점과 끝점
    private lateinit var startPoint: Location
    private lateinit var endPoint: Location

    private lateinit var mBroadcastReceiver: MBroadcastReceiver // 브로드캐스트 리시버

    private lateinit var beforeLatLng: LatLng // 선 긋기 시작 위치

    private lateinit var canStartAreaCircle: Circle // 시작 가능한 반경 원

    private lateinit var job: Job // 코루틴 동작을 제어하기 위한 job

    private var mLocationMarker: Marker? = null // 내 위치 마커
    private var opponentLocationMarker: Marker? = null // 상대 위치 마커

    private var inCanStartArea = false // 시작 가능한 범위 내에 있는지

    private var gotFirstLocation = false // 첫 번째 위치를 받아와 시작 가능한 상태인지

    private var second = 0 // 초

    private lateinit var latLngList: ArrayList<LatLng> // 폴리라인 만들 위치 리스트
    private lateinit var opponentLatLngList: ArrayList<LatLng> // 상대 폴리라인 만들 위치 리스트

    private lateinit var mPolyline: Polyline // 내가 달린 구간 폴리라인
    private lateinit var opponentPolyline: Polyline // 상대 달린 구간 폴리라인

    // 트랙 관련
    private lateinit var trackPolyline: Polyline
    private var trackStartPointMarker: Marker? = null // 트랙 시작점 마커
    private var trackEndPointMarker: Marker? = null // 트랙 끝점 마커

    // 내마커 아이콘
    private lateinit var myMarkerIcon: View // 커스텀 마커 뷰
    private lateinit var myMarkerIconTextView: TextView // 커스텀 마커 텍스트 뷰
    private lateinit var myMarkerUserNameTextView: TextView // 내 이름 텍스트 뷰

    // 상대 마커 아이콘
    private lateinit var opponentMarkerIcon: View // 커스텀 마커 뷰
    private lateinit var opponentMarkerIconTextView: TextView // 커스텀 마커 텍스트 뷰
    private lateinit var opponentMarkerUserNameTextView: TextView // 상대 이름 텍스트 뷰

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityTrackPaceMakeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // 체크포인트 커스텀 다이얼로그 초기화
        checkpointDialog = Dialog(this)
        checkpointDialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 제거
        checkpointDialog.setContentView(R.layout.checkpoint_dialog)

        // 완료시 커스텀 다이얼로그 초기화
        completeRecordDialog = Dialog(this)
        completeRecordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 제거
        completeRecordDialog.setContentView(R.layout.complete_record_dialog)
        completeRecordDialog.setCancelable(false) // 화면 밖 터치해서 종료 방지

        latLngList = ArrayList()
        opponentLatLngList = ArrayList()
        checkpointMarkerList = ArrayList()

        // 인텐트로 넘어온 옵션값 받음
        val intent = intent
        exerciseKind = intent.getStringExtra("exerciseKind")!!
        matchType = intent.getStringExtra("matchType")!!
        trackId = intent.getStringExtra("trackId")!!
        opponentGpsDataId = intent.getStringExtra("opponentGpsDataId")!!
        opponentPostId = intent.getIntExtra("opponentPostId", 0)!!
        opponentAvgSpeed = intent.getDoubleExtra("opponentAvgSpeed", 0.0)
        opponentTime = intent.getIntExtra("opponentTime", 0)
        println(" (TrackPaceMake) exerciseKind $exerciseKind")
        println(" (TrackPaceMake) matchType $matchType")
        println(" (TrackPaceMake) trackId $trackId")
        println(" (TrackPaceMake) opponentGpsDataId $opponentGpsDataId")
        println(" (TrackPaceMake) opponentPostId $opponentPostId")

        textToSpeech = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.JAPANESE

                textToSpeechInitialized = true
            }
        }

        // db 사용 설정
        val db = AppDatabase.getInstance(applicationContext)!!
        gpsDataDao = db.gpsDataDao()
        opponentGpsDataDao = db.opponentGpsDataDao()

        job = Job() // job 생성

        initRetrofit()

        // 마커 아이콘 초기화
        myMarkerIcon = LayoutInflater.from(this).inflate(R.layout.user_icon, null)!!
        myMarkerIconTextView = myMarkerIcon.findViewById(R.id.marker_icon_speed) as TextView
        myMarkerUserNameTextView = myMarkerIcon.findViewById(R.id.marker_user_name) as TextView
        myMarkerUserNameTextView.text = "나"
        val myMarkerIconImageView = myMarkerIcon.findViewById(R.id.marker_icon_image) as ImageView
        myMarkerIconImageView.setImageResource(R.drawable.ic_my_location_marker)

        opponentMarkerIcon = LayoutInflater.from(this).inflate(R.layout.user_icon, null)!!
        opponentMarkerIconTextView = opponentMarkerIcon.findViewById(R.id.marker_icon_speed) as TextView
        opponentMarkerUserNameTextView = opponentMarkerIcon.findViewById(R.id.marker_user_name) as TextView
        val opponentMarkerIconImageView = opponentMarkerIcon.findViewById(R.id.marker_icon_image) as ImageView
        opponentMarkerIconImageView.setImageResource(R.drawable.ic_opponent_location_marker)

        // 액티비티 이동 후 답을 받는 콜백
        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // 정상적으로 카운트다운 다 지나오면 시작
            if (result.resultCode == CountDownActivity.COUNT_DOWN_ACTIVITY_RESULT_CODE) {

                // 커맨드 보냄 (서비스는 한번 더 실행 안되니 커맨드가 보내진다.)
                val intent = Intent(this@TrackPaceMakeActivity, TrackPaceMakeService::class.java)
                intent.action = TrackPaceMakeService.START_RECORD
                startForegroundService(intent)

                // 버튼 바꿈
                binding.startButton.visibility = View.GONE
                binding.stopButton.visibility = View.VISIBLE

                // 시작 영역 없앰
                canStartAreaCircle.remove()

                // 기존 트랙은 투명하게
                trackPolyline.color = resources.getColor(R.color.no_selected_polyline_color, null)
                trackEndPointMarker?.alpha = 0.4F
                trackStartPointMarker?.alpha = 0.4F

                binding.tvPaceMake.visibility = View.VISIBLE // 페이스메이커와의 차이 정보 보이게
            }
        }

        // 시작 버튼 초기화
        binding.startButton.setOnClickListener{
            println("시작 버튼 클릭함")

            if (inCanStartArea) {
                val intent = Intent(this, CountDownActivity::class.java)
                activityResultLauncher.launch(intent)
            } else {
                Toast.makeText(this@TrackPaceMakeActivity, "시작 가능 위치가 아닙니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 종료 버튼 초기화
        binding.stopButton.setOnClickListener {
            println("종료 버튼 클릭함")
            AlertDialog.Builder(this)
                .setTitle("기록 종료")
                .setMessage("정말로 기록을 종료하시겠습니까")
                .setPositiveButton("취소") { _, _ ->
                }
                .setNegativeButton("종료") { _, _ ->
                    // 기록 종료하는 경우
                    stopRecord()
                }
                .show()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // 서비스 종료하라고 커맨드 보냄
    @SuppressLint("NewApi")
    private fun stopRecord() {
        val intent = Intent(this@TrackPaceMakeActivity, TrackPaceMakeService::class.java)
        intent.action = TrackPaceMakeService.STOP_SERVICE
        startForegroundService(intent)
        super.onBackPressed()
    }

    override fun onBackPressed() {
        println("onBackPressed 호출")
        if (!TrackPaceMakeService.isStarted) {
            stopRecord()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver)
        _binding = null
    }


    @SuppressLint("NewApi")
    private fun startProcess() {
        CoroutineScope(Dispatchers.Main + job).launch {
            launch {
                initTrack()
            }.join() // 트랙 초기화하는거 기다리고 다음 작업 수행함

            binding.tvInformation.setBackgroundColor(resources.getColor(R.color.green))
            binding.tvInformation.text = "위치 정보 불러오는 중"

            if (TrackPaceMakeService.isStarted) {
                println("trackPaceMake 이미 실행중이다.")
                // 버튼 바꿈
                binding.startButton.visibility = View.GONE
                binding.stopButton.visibility = View.VISIBLE

                binding.tvInformation.visibility = View.GONE // 정보 창 없앰

                // 시작 영역 없앰
                canStartAreaCircle.remove()

                trackPolyline.color = resources.getColor(R.color.no_selected_polyline_color, null)
                trackEndPointMarker?.alpha = 0.4F
                trackStartPointMarker?.alpha = 0.4F

                // 위치 가져오고 내 마커 생성
                beforeLatLng = LatLng(TrackPaceMakeService.mLocation!!.latitude, TrackPaceMakeService.mLocation!!.longitude)
                mLocationMarker = mGoogleMap.addMarker(MarkerOptions()
                    .position(beforeLatLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(Utils.createBitmapFromView(myMarkerIcon)))
                    .anchor(0.5F, 0.5F))

                // 상대 위치 마커 생성
                opponentMarkerUserNameTextView.text = TrackPaceMakeService.opponentUserName
                opponentLocationMarker = mGoogleMap.addMarker(MarkerOptions()
                    .position(LatLng(TrackPaceMakeService.opponentLocation!!.latitude, TrackPaceMakeService.opponentLocation!!.longitude))
                    .icon(BitmapDescriptorFactory.fromBitmap(Utils.createBitmapFromView(opponentMarkerIcon)))
                    .anchor(0.5F, 0.5F))

                // 통과한 체크포인트는 바꿔줌
                for (i in 0 until TrackPaceMakeService.checkpointIndex) {
                    checkpointMarkerList[i].setIcon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.checkpoint_after,null)))
                }

                binding.tvPaceMake.visibility = View.VISIBLE // 페이스메이크 창 보이게

                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(beforeLatLng, 18.0f)) // 화면 이동

                loadGpsDataFromDatabaseAndDrawPolyline()
            } else {
                registerLocalBroadcastReceiver()

                // 서비스 시작
                val intent = Intent(this@TrackPaceMakeActivity, TrackPaceMakeService::class.java)
                intent.action = TrackPaceMakeService.START_PROCESS
                intent.putExtra("exerciseKind", exerciseKind)
                intent.putExtra("trackName", track.trackName)
                intent.putExtra("trackId", track._id)
                intent.putExtra("matchType", matchType)
                intent.putExtra("opponentPostId", opponentPostId)
                intent.putExtra("opponentGpsDataId", opponentGpsDataId)
                intent.putExtra("opponentAvgSpeed", opponentAvgSpeed)
                intent.putExtra("opponentTime", opponentTime)
                startForegroundService(intent)
            }
        }
    }

    // 중간에 액티비티 재실행 시 db에 있는 gps 데이터 다시 가져오고 폴리라인 그려줌
    @SuppressLint("NewApi")
    private fun loadGpsDataFromDatabaseAndDrawPolyline() {
        CoroutineScope(Dispatchers.Main + job).launch {
            val gpsDataList = withContext(Dispatchers.IO) {
                gpsDataDao.getAllGpsData() // withContext 의 반환값
            }

            println("db 에서 불러온 크기: ${gpsDataList.size}")

            // 선 그리기
            latLngList = withContext(Dispatchers.Default) {
                val latLngListInner = ArrayList<LatLng>()
                for (gpsData in gpsDataList) {
                    latLngListInner.add(LatLng(gpsData.lat, gpsData.lng))
                    println("withContext 내부 for 문 수행 중")
                }
                latLngListInner
            }

            mGoogleMap.addMarker(MarkerOptions()
                .position(latLngList[0])
                .icon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.record_start_point, null)))
                .anchor(0.5F, 0.5F))

            mPolyline = mGoogleMap.addPolyline(PolylineOptions()
                .addAll(latLngList)
                .color(resources.getColor(R.color.mainColor, null))
                .width(12F)) // 그림 그림

            val opponentGpsDataList = withContext(Dispatchers.IO) {
                opponentGpsDataDao.getOpponentGpsDataUntilSecond(second)
            }

            // 선 그리기
            opponentLatLngList = withContext(Dispatchers.Default) {
                val latLngListInner = ArrayList<LatLng>()
                for (gpsData in opponentGpsDataList) {
                    latLngListInner.add(LatLng(gpsData.lat, gpsData.lng))
                    println("withContext 내부 for 문 수행 중")
                }
                latLngListInner
            }

            opponentPolyline = mGoogleMap.addPolyline(PolylineOptions()
                .addAll(latLngList)
                .color(resources.getColor(R.color.opponent_polyline_color, null))
                .width(12F)) // 그림 그림

            registerLocalBroadcastReceiver()
        }
    }

    private fun registerLocalBroadcastReceiver() {
        mBroadcastReceiver = MBroadcastReceiver()
        LocalBroadcastManager.getInstance(this@TrackPaceMakeActivity).registerReceiver(mBroadcastReceiver, IntentFilter(
            TrackPaceMakeService.ACTION_BROADCAST))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        mGoogleMap.setMaxZoomPreference(18F) // 최대 줌

        checkPermission()
    }

    @SuppressLint("NewApi")
    private fun predictLocation() {
        println("내 위치 바뀌는 거: ${TrackPaceMakeService.myBeforeLocationChangedSecond} $second")

        // 내 위치 예측
        var myIncreaseSumDistance = 0F // 한계치까지만 비교하기 위한 내 위치 증가깂

        var myBeforeLocation = Location("start") // 트랙 위에서 현재 내 위치라고 예상되는 지점
        myBeforeLocation.latitude =
            track.gps.coordinates[TrackPaceMakeService.myLocationIndexOnTrack][1]
        myBeforeLocation.longitude =
            track.gps.coordinates[TrackPaceMakeService.myLocationIndexOnTrack][0]

        var myMinDistance =
            TrackPaceMakeService.mLocation!!.distanceTo(myBeforeLocation) // 가장 작은 거리 차이인 지점과의 거리차이

        var myPredictedLocation = TrackPaceMakeService.myLocationIndexOnTrack // 예상하는 트랙위의 내 위치

        // 이동할 수 있는 가장 최대 거리라고 생각하는 지점까지 반복하며 트랙 위에서 어디와 가장 가까운지 구함
        println("$myPredictedLocation ${track.gps.coordinates.size}")
        for (i in myPredictedLocation + 1 until track.gps.coordinates.size) {
            var location = Location("flag")
            location.latitude = track.gps.coordinates[i][1]
            location.longitude = track.gps.coordinates[i][0]

            // 이동할 수 있는 가장 최대 거리를 넘어서면 반복문 종료
            myIncreaseSumDistance += myBeforeLocation.distanceTo(location)
            if (myIncreaseSumDistance / (second - TrackPaceMakeService.myBeforeLocationChangedSecond) >= TrackPaceMakeService.MAX_DISTANCE) {
                break
            }

            val distance = location.distanceTo(TrackPaceMakeService.mLocation)
            if (distance <= myMinDistance) {
                myMinDistance = distance
                myPredictedLocation = i
            }

            myBeforeLocation = location
        }

        // 내 트랙 위에서의 누적 이동 거리 갱신해줌
        for (i in TrackPaceMakeService.myLocationIndexOnTrack until myPredictedLocation) {
            val beforeLocation = Location("before")
            beforeLocation.latitude = track.gps.coordinates[i][1]
            beforeLocation.longitude = track.gps.coordinates[i][0]
            val afterLocation = Location("after")
            afterLocation.latitude = track.gps.coordinates[i + 1][1]
            afterLocation.longitude = track.gps.coordinates[i + 1][0]
            TrackPaceMakeService.mySumDistanceOnTrack += beforeLocation.distanceTo(afterLocation)
        }

        TrackPaceMakeService.myLocationIndexOnTrack = myPredictedLocation // 내 예상 지점 갱신

        TrackPaceMakeService.myBeforeLocationChangedSecond = second
    }

    private fun predictOpponentLocation() {
        var opponentIncreaseSumDistance = 0F // 한계치까지만 비교하기 위한 상대 위치 증가깂

        var opponentBeforeLocation = Location("start") // 트랙 위에서 현재 내 위치라고 예상되는 지점
        opponentBeforeLocation.latitude =
            track.gps.coordinates[TrackPaceMakeService.opponentLocationIndexOnTrack][1]
        opponentBeforeLocation.longitude =
            track.gps.coordinates[TrackPaceMakeService.opponentLocationIndexOnTrack][0]

        var opponentMinDistance =
            TrackPaceMakeService.opponentLocation!!.distanceTo(opponentBeforeLocation) // 가장 작은 거리 차이인 지점과의 거리차이

        var opponentPredictedLocation =
            TrackPaceMakeService.opponentLocationIndexOnTrack // 예상하는 트랙위의 내 위치

        // 1초안에 이동할 수 있는 가장 최대 거리라고 생각하는 지점까지 반복하며 트랙 위에서 어디와 가장 가까운지 구함
        for (i in opponentPredictedLocation + 1 until track.gps.coordinates.size) {
            var location = Location("flag")
            location.latitude = track.gps.coordinates[i][1]
            location.longitude = track.gps.coordinates[i][0]

            // 1초안에 이동할 수 있는 가장 최대 거리를 넘어서면 반복문 종료
            opponentIncreaseSumDistance += opponentBeforeLocation.distanceTo(location)
            if (opponentIncreaseSumDistance / (second - TrackPaceMakeService.opponentBeforeLocationChangedSecond) >= TrackPaceMakeService.MAX_DISTANCE) {
                break
            }

            val distance = location.distanceTo(TrackPaceMakeService.opponentLocation)
            if (distance <= opponentMinDistance) {
                opponentMinDistance = distance
                opponentPredictedLocation = i
            }

            opponentBeforeLocation = location
        }

        // 상대 트랙 위에서의 누적 이동 거리 갱신해줌
        for (i in TrackPaceMakeService.opponentLocationIndexOnTrack until opponentPredictedLocation) {
            val beforeLocation = Location("before")
            beforeLocation.latitude = track.gps.coordinates[i][1]
            beforeLocation.longitude = track.gps.coordinates[i][0]
            val afterLocation = Location("after")
            afterLocation.latitude = track.gps.coordinates[i + 1][1]
            afterLocation.longitude = track.gps.coordinates[i + 1][0]
            TrackPaceMakeService.opponentSumDistanceOnTrack += beforeLocation.distanceTo(
                afterLocation
            )
        }

        TrackPaceMakeService.opponentLocationIndexOnTrack = opponentPredictedLocation // 상대 예상 지점 갱신
        TrackPaceMakeService.opponentBeforeLocationChangedSecond = second
    }

    // 트랙 정보 가져와서 그림
    @SuppressLint("NewApi")
    private suspend fun initTrack() {
        val token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!
        val trackResponse = supplementService.getTrack(token, trackId)
        println("응답코드: ${trackResponse.code()}")

        if (trackResponse.isSuccessful) {
            println("응답: ${trackResponse.body()}")
            track = trackResponse.body()!!

            //출발점 초기화
            startPoint = Location("startPoint")
            startPoint.latitude = track.gps.coordinates[0][1]
            startPoint.longitude = track.gps.coordinates[0][0]

            // 도착점 초기화
            endPoint = Location("endPoint")
            endPoint.latitude = track.gps.coordinates[track.gps.coordinates.size - 1][1]
            endPoint.longitude = track.gps.coordinates[track.gps.coordinates.size - 1][0]

            // 경로 그림
            val trackLatLngList = ArrayList<LatLng>()
            for (coordinate in track.gps.coordinates) {
                trackLatLngList.add(LatLng(coordinate[1], coordinate[0]))
            }
            trackPolyline = mGoogleMap.addPolyline(
                PolylineOptions()
                .clickable(true)
                .addAll(trackLatLngList)
                .color(ContextCompat.getColor(this, R.color.main_color))
                .width(12F))

            // 체크포인트 추가
            for (checkpointIndex in track.checkPoint) {
                val checkpointMarker = mGoogleMap.addMarker(
                    MarkerOptions()
                    .position(LatLng(track.gps.coordinates[checkpointIndex][1], track.gps.coordinates[checkpointIndex][0]))
                    .title("체크포인트")
                    .icon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.checkpoint_before,null)))
                    .anchor(0.5F, 0.5F))!!

                checkpointMarkerList.add(checkpointMarker)
            }

            // 출발점 마커 추가
            trackStartPointMarker = mGoogleMap.addMarker(
                MarkerOptions()
                .position(LatLng(startPoint.latitude, startPoint.longitude))
                .title("출발점")
                .icon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.start_point_marker,null)))
                .anchor(0.5F, 0.9F))

            // 시작 가능 반경 그림
            canStartAreaCircle = mGoogleMap.addCircle(
                CircleOptions()
                .center(LatLng(startPoint.latitude, startPoint.longitude))
                .radius(20.0)
                .fillColor(resources.getColor(R.color.default_marker_color_opacity, null))
                .strokeWidth(0F))

            // 도착점 마커 추가
            trackEndPointMarker = mGoogleMap.addMarker(
                MarkerOptions()
                .position(LatLng(track.gps.coordinates[track.gps.coordinates.size - 1][1], track.gps.coordinates[track.gps.coordinates.size - 1][0]))
                .title("도착점")
                .icon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.end_point_marker,null)))
                .anchor(0.25F, 0.9F))

            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(track.gps.coordinates[0][1], track.gps.coordinates[0][0]), 18.0f)) // 화면 이동

            println("다 그림")
        } else {
            println("경로 초기화 실패")
        }

        println("다 그림")
    }

    // 권한 확인
    private fun checkPermission() {
        when {
            // 위치 권한 다 허용돼 있으면 시작
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                startProcess()
            }
            // 위치 하나라도 거부한 상태면 true 를 반환
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }
            else -> {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }
        }
    }

    // 사용자가 권한 요청에 응답했을 때의 콜백
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // 요청 코드에 따라 분기처리
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                println("위치 권한 요청 응답 옴")
                if (grantResults.isNotEmpty()) {

                    // 사용자가 permission 요청에 올바른 응답을 했는지 확인
                    var granted = true
                    for (grantResult in grantResults) {
                        println("위치 권한 요청 응답: ${grantResult == PackageManager.PERMISSION_GRANTED}")
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            granted = false
                        }
                    }
                    if (granted) {
                        startProcess()
                    } else {
                        Toast.makeText(this@TrackPaceMakeActivity, "위치 권한이 없어 해당 기능을 이용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    // 레트로핏 초기화
    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java)
    }

    // 브로드캐스트를 받을 리시버
    inner class MBroadcastReceiver: BroadcastReceiver() {
        @SuppressLint("NewApi")
        override fun onReceive(context: Context?, intent: Intent?) {

            // flag 에 따라 분기처리
            when (intent?.getStringExtra("flag")) {
                TrackPaceMakeService.BEFORE_START_LOCATION_UPDATE -> { // 시작 전 위치 업데이트
                    val latLng = intent?.getParcelableExtra<LatLng>(TrackPaceMakeService.LAT_LNG)!!
                    println("리시버로 위치 받음 ${latLng.latitude}, ${latLng.longitude}")

                    if (!gotFirstLocation) {
                        gotFirstLocation = true

                        // 내 위치 마커 생성
                        mLocationMarker = mGoogleMap.addMarker(MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromBitmap(Utils.createBitmapFromView(myMarkerIcon))))

                        binding.tvInformation.text = "시작 가능 위치로 이동하세요."
                        binding.tvInformation.setBackgroundColor(resources.getColor(R.color.red))
                    } else {
                        mLocationMarker?.position = latLng // 마커 이동
                    }

                    // 시작 가능 위치인지 확인
                    val mLocation = Location("myLocation")
                    mLocation.latitude = latLng.latitude
                    mLocation.longitude = latLng.longitude

//                    inCanStartArea = mLocation.distanceTo(startPoint) < 20.0
                    inCanStartArea = true
                    println("시작 가능 위치 내인지: $inCanStartArea")
                    if (inCanStartArea) {
                        binding.tvInformation.visibility = View.GONE
                    } else {
                        binding.tvInformation.visibility = View.VISIBLE
                    }
                }
                TrackPaceMakeService.RECORD_START_LAT_LNG -> { // 기록 시작 위치
                    println("업데이트 시작 위치 받음")
                    // 내 기록 시작 위치 받음.
                    val recordStartLatLng = intent?.getParcelableExtra<LatLng>(TrackPaceMakeService.LAT_LNG)!!
                    latLngList.add(recordStartLatLng)

                    mGoogleMap.addMarker(MarkerOptions()
                        .position(recordStartLatLng)
                        .icon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.record_start_point, null)))
                        .anchor(0.5F, 0.5F))

                    mPolyline = mGoogleMap.addPolyline(PolylineOptions()
                        .addAll(latLngList)
                        .color(resources.getColor(R.color.mainColor, null))
                        .width(12F)) // 그림 그림

                    beforeLatLng = recordStartLatLng
                }

                TrackPaceMakeService.AFTER_START_UPDATE -> { // 기록 시작 후 초마다 받는 업데이트
                    CoroutineScope(Dispatchers.Main).launch {
                        second = intent?.getIntExtra(TrackPaceMakeService.SECOND, 0)
                        binding.tvTime.text = Utils.timeToText(second)

                        val avgSpeed = intent?.getDoubleExtra(TrackPaceMakeService.AVG_SPEED, 0.0)
                        binding.tvAvgSpeed.text = Utils.formatDoublePointTwo(avgSpeed)

                        val calorie = intent?.getDoubleExtra(TrackPaceMakeService.CALORIE, 0.0)
                        binding.tvKcal.text = Utils.formatDoublePointTwo(calorie)

                        // 상대 위치 바꼈을 떄
                        val opponentLocationChanged = intent?.getBooleanExtra(TrackPaceMakeService.OPPONENT_LOCATION_CHANGED, true)
                        if (opponentLocationChanged) {
                            val opponentSpeed = intent?.getFloatExtra(TrackPaceMakeService.OPPONENT_SPEED, 0F)
                            opponentMarkerIconTextView.text = Utils.speedToText(opponentSpeed)
                            opponentLocationMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(Utils.createBitmapFromView(opponentMarkerIcon))) // 속도 변경

                            val opponentLatLng = intent?.getParcelableExtra<LatLng>(
                                TrackPaceMakeService.OPPONENT_LAT_LNG)!! // 상대 위치
                            opponentLocationMarker?.position = opponentLatLng // 상대 마커 이동

                            // 상대 폴리라인 갱신
                            opponentLatLngList.add(opponentLatLng)
                            opponentPolyline.points = opponentLatLngList
                        }

                        // 내 위치 바꼈을 때
                        val locationChanged = intent?.getBooleanExtra(TrackPaceMakeService.LOCATION_CHANGED, true)
                        if (locationChanged) {
                            val latLng = intent?.getParcelableExtra<LatLng>(TrackPaceMakeService.LAT_LNG)!!

                            val distance = intent?.getDoubleExtra(TrackPaceMakeService.DISTANCE, 0.0)
                            binding.tvDistance.text = String.format("%.2f", distance)

                            val speed = intent?.getFloatExtra(TrackPaceMakeService.SPEED, 0F)
                            myMarkerIconTextView.text = Utils.speedToText(speed)
                            mLocationMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(Utils.createBitmapFromView(myMarkerIcon)))

                            mLocationMarker?.position = latLng // 마커 이동
                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mGoogleMap.cameraPosition.zoom)) // 화면 이동

                            // 폴리라인 갱신
                            latLngList.add(beforeLatLng)
                            mPolyline.points = latLngList

                            beforeLatLng = latLng
                        }
                        // 뭔가 위치 바뀐거 있으면 서로간의 거리 다시 구함.
                        if (locationChanged || opponentLocationChanged) {
                            // 내 위치가 바꼈으면 검사
                            if (locationChanged) {
                                launch(Dispatchers.Default) {
                                    predictLocation()
                                }.join()

                                 // 예상 지점
//                                mGoogleMap.addMarker(MarkerOptions().position(LatLng(track.gps.coordinates[TrackPaceMakeService.myLocationIndexOnTrack][1], track.gps.coordinates[TrackPaceMakeService.myLocationIndexOnTrack][0])))

                                // 끝에 도착했는지 여기서 체크하자
                                if (TrackPaceMakeService.myLocationIndexOnTrack == track.gps.coordinates.size - 1) {
                                    // 종료하기전 스냅샷 찍음
                                    val builder: LatLngBounds.Builder = LatLngBounds.Builder() // 카메라 이동을 위한 빌더
                                    for (latLng in latLngList) {
                                        builder.include(latLng) // 카메라안에 들어와야 하는 지점들 추가
                                    }
                                    // 카메라 업데이트
                                    val bounds: LatLngBounds = builder.build()
                                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))

                                    mGoogleMap.snapshot(this@TrackPaceMakeActivity)
                                }

                                // 체크포인트 있으면 검사
                                if (track.checkPoint.size > TrackPaceMakeService.checkpointIndex) {
                                    if (TrackPaceMakeService.myLocationIndexOnTrack >= track.checkPoint[TrackPaceMakeService.checkpointIndex]) {
                                        val token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!
                                        val checkpointResponse = supplementService.checkpoint(token, TrackPaceMakeService.checkpointIndex, trackId, second)
                                        if (checkpointResponse.isSuccessful) {
                                            // 다이얼로그 띄움
                                            checkpointDialog.checkpoint_pace.text = "上位${checkpointResponse.body()!!.rank.toInt()}パーセントのペースです。"
                                            checkpointDialog.show()
                                            checkpointDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                            Handler(mainLooper).postDelayed({
                                                checkpointDialog.dismiss()
                                            }, 6000)
                                            if (textToSpeechInitialized) {
                                                textToSpeech.speak("チェックポイントを通過しました。上位${checkpointResponse.body()!!.rank.toInt()}パーセントのペースです。", TextToSpeech.QUEUE_ADD, null, "abc")
                                            }
                                        }
                                        checkpointMarkerList[TrackPaceMakeService.checkpointIndex].setIcon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.checkpoint_after,null)))
                                        TrackPaceMakeService.checkpointIndex += 1
                                    }
                                }
                            }

                            if (opponentLocationChanged) {
                                launch(Dispatchers.Default) {
                                    predictOpponentLocation()
                                }.join()
                            }
                            val predictLocationDifference = TrackPaceMakeService.mySumDistanceOnTrack - TrackPaceMakeService.opponentSumDistanceOnTrack
                            if (predictLocationDifference >= 0) {
                                binding.tvPaceMake.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.direction_north, null), null, null, null)
                                binding.tvPaceMake.text = "${TrackPaceMakeService.opponentUserName}より約${predictLocationDifference.toInt()}m前"
                            } else {
                                binding.tvPaceMake.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.direction_south, null), null, null, null)
                                binding.tvPaceMake.text = "${TrackPaceMakeService.opponentUserName}より約${predictLocationDifference.toInt()*-1}m後ろ"
                            }
                        }
                        // 30초간격으로 음성 알림
                        println("내 거리: ${TrackPaceMakeService.mySumDistanceOnTrack}, 상대 거리: ${TrackPaceMakeService.opponentSumDistanceOnTrack}")
                        if (textToSpeechInitialized && (second % 30 == 0)) { // 초기화된 상태일때 30초 간격으로 페이스에 대해 음성 안내
                            val predictLocationDifference = TrackPaceMakeService.mySumDistanceOnTrack - TrackPaceMakeService.opponentSumDistanceOnTrack
                            if (predictLocationDifference >= 0) {
                                textToSpeech.speak("${TrackPaceMakeService.opponentUserName}より やく ${predictLocationDifference.toInt()}メートル　まえです。", TextToSpeech.QUEUE_ADD, null, "abc")
                            } else {
                                textToSpeech.speak("${TrackPaceMakeService.opponentUserName}より やく ${predictLocationDifference.toInt()*-1}メートル　うしろです。", TextToSpeech.QUEUE_ADD, null, "abc")
                            }
                        }
                    }

                }
                TrackPaceMakeService.OPPONENT_START_LAT_LNG -> { // 상대 시작 위치
                    val opponentLatLng = intent?.getParcelableExtra<LatLng>(TrackPaceMakeService.OPPONENT_LAT_LNG)!!
                    opponentLatLngList.add(opponentLatLng)

                    opponentPolyline = mGoogleMap.addPolyline(PolylineOptions()
                        .addAll(latLngList)
                        .color(resources.getColor(R.color.opponent_polyline_color, null))
                        .width(12F)) // 그림 그림

                    // 상대 위치 마커 생성
                    opponentMarkerUserNameTextView.text = TrackPaceMakeService.opponentUserName
                    opponentLocationMarker = mGoogleMap.addMarker(MarkerOptions()
                        .position(opponentLatLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(Utils.createBitmapFromView(opponentMarkerIcon)))
                    )
                }
            }
        }
    }

    // 스냅샷 찍었을 때 콜백
    @SuppressLint("NewApi")
    override fun onSnapshotReady(snapshot: Bitmap?) {
        //앱 내부 cache 저장소: /data/user/0/com.example.capstonandroid/cache

        val completeRecordIntent = Intent(this@TrackPaceMakeActivity, TrackPaceMakeService::class.java)
        completeRecordIntent.action = TrackPaceMakeService.COMPLETE_RECORD
        startForegroundService(completeRecordIntent)

        // 이미 있는 이미지는 삭제
        val file = cacheDir
        val fileList = file.listFiles()
        for (file in fileList) {
            if (file.name == "map.png") {
                file.delete()
            }
        }

        // 이미지 저장
        val fileOutputStream = FileOutputStream("${cacheDir}/map.png")
        snapshot?.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.close()
        println("이미지 저장 끝남")

        completeRecordDialog.tv_opponent_name.text = TrackPaceMakeService.opponentUserName
        completeRecordDialog.tv_my_avg_speed.text = "${binding.tvAvgSpeed.text}km/h"
        completeRecordDialog.tv_opponent_avg_speed.text = "${TrackPaceMakeService.opponentAvgSpeed}km/h"
        completeRecordDialog.tv_my_time.text = Utils.timeToText(second)
        completeRecordDialog.tv_opponent_time.text = Utils.timeToText(TrackPaceMakeService.opponentTime)
        completeRecordDialog.show()
        completeRecordDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        completeRecordDialog.btn_upload.setOnClickListener {
            val uploadPostIntent = Intent(this@TrackPaceMakeActivity, TrackPaceMakeService::class.java)
            uploadPostIntent.action = TrackPaceMakeService.UPLOAD_POST
            startForegroundService(uploadPostIntent)
            finish()
        }


    }
}