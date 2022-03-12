package com.example.capstonandroid.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.capstonandroid.R
import com.example.capstonandroid.TrackRecordService
import com.example.capstonandroid.Utils
import com.example.capstonandroid.databinding.ActivityTrackRecordBinding
import com.example.capstonandroid.network.dto.Track
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class TrackRecordActivity : AppCompatActivity(), OnMapReadyCallback {
    private var _binding: ActivityTrackRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var exerciseKind: String
    private lateinit var matchType: String
    private lateinit var trackId: String

    private lateinit var retrofit: Retrofit // 레트로핏 인스턴스
    private lateinit var supplementService: BackendApi // api

    private lateinit var mGoogleMap: GoogleMap

    private lateinit var track: Track

    private lateinit var mLocation: Location // 내 위치

    private lateinit var startPoint: Location // 시작점

    private lateinit var mBroadcastReceiver: MBroadcastReceiver // 브로드캐스트 리시버

    private lateinit var beforeLocation: Location // 선 긋기 시작 위치

    private lateinit var canStartCircle: Circle // 시작 가능한 반경 원

    private lateinit var job: Job // 코루틴 동작을 제어하기 위한 job

    private var mLocationMarker: Marker? = null // 내 위치 마커

    private var started = false // 기록 시작됐는지

    private var inCanStartArea = false // 시작 가능한 범위 내에 있는지

    private var isStarted = false // 시작 했는지

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityTrackRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트로 넘어온 옵션값 받음
        val intent = intent
        exerciseKind = intent.getStringExtra("exerciseKind")!!
        matchType = intent.getStringExtra("matchType")!!
        trackId = intent.getStringExtra("trackId")!!
        println("exerciseKind $exerciseKind")
        println("matchType $matchType")
        println("trackId $trackId")

        job = Job() // job 생성

        initRetrofit()

        // 시작 버튼 초기화
        binding.startButton.setOnClickListener{
            println("시작 버튼 클릭함")

            if (inCanStartArea) {
                // 커맨드 보냄 (서비스는 한번 더 실행 안되니 커맨드가 보내진다.)
                val intent = Intent(this@TrackRecordActivity, TrackRecordService::class.java)
                intent.action = TrackRecordService.START_RECORD
                startForegroundService(intent)

                isStarted = true

                // 시작한 상태 저장
                getSharedPreferences("trackRecord", MODE_PRIVATE)
                    .edit()
                    .putString("exerciseKind", exerciseKind)
                    .putString("matchType", matchType)
                    .putBoolean("isStarted", true)
                    .commit()

                // 버튼 바꿈
                binding.startButton.visibility = View.GONE
                binding.stopButton.visibility = View.VISIBLE
            } else {
                Toast.makeText(this@TrackRecordActivity, "시작 가능 위치가 아닙니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 종료 버튼 초기화
        binding.stopButton.setOnClickListener {
            println("종료 버튼 클릭함")
            val intent = Intent(this@TrackRecordActivity, TrackRecordService::class.java)
            intent.action = TrackRecordService.COMPLETE_RECORD
            startForegroundService(intent)

            finish()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("NewApi")
    override fun onBackPressed() {
        println("onBackPressed 호출")

        // 달리기중 아닐떄만 뒤로 갈 수 있게 함
        if (!isStarted) {
            // 서비스 종료하라고 커맨드 보냄
            val intent = Intent(this@TrackRecordActivity, TrackRecordService::class.java)
            intent.action = TrackRecordService.STOP_SERVICE
            startForegroundService(intent)
            super.onBackPressed()
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
            val initTrackJob = launch {
                initTrack()
            }
            initTrackJob.join()

            binding.tvInformation.setBackgroundColor(R.color.green)
            binding.tvInformation.text = "위치 정보 불러오는 중"

            // 브로드캐스트 리시버 초기화
            mBroadcastReceiver = MBroadcastReceiver()
            LocalBroadcastManager.getInstance(this@TrackRecordActivity).registerReceiver(mBroadcastReceiver, IntentFilter(TrackRecordService.ACTION_BROADCAST))

            // 서비스 시작
            val intent = Intent(this@TrackRecordActivity, TrackRecordService::class.java)
            intent.action = TrackRecordService.START_PROCESS
            startForegroundService(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        checkPermission()
    }

    // 트랙 정보 가져와서 그림
    private suspend fun initTrack() {
        val trackResponse = supplementService.getTrack("http://13.124.24.179/api/track/${trackId}")
        println("응답코드: ${trackResponse.code()}")

        if (trackResponse.isSuccessful) {
            println("응답: ${trackResponse.body()}")
            track = trackResponse.body()!!
            startPoint = Location("startPoint")
            startPoint.latitude = track.start_latlng[1]
            startPoint.longitude = track.start_latlng[0]

            // 경로 그림
            val latLngList = ArrayList<LatLng>()
            for (coordinate in track.gps.coordinates) {
                latLngList.add(LatLng(coordinate[1], coordinate[0]))
                println("${coordinate[1]}, ${coordinate[0]}")
            }
            mGoogleMap.addPolyline(PolylineOptions()
                .clickable(true)
                .addAll(latLngList)
                .color(R.color.main_color)
                .width(10F))

            // 출발점 마커 추가
            mGoogleMap.addMarker(MarkerOptions()
                .position(LatLng(startPoint.latitude, startPoint.longitude))
                .title("출발점")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_marker))
                .anchor(0.5F, 1F))

            // 시작 가능 반경 그림
            canStartCircle = mGoogleMap.addCircle(CircleOptions()
                .center(LatLng(startPoint.latitude, startPoint.longitude))
                .radius(20.0)
                .fillColor(R.color.area_color)
                .strokeWidth(0F))

            // 도착점 마커 추가
            mGoogleMap.addMarker(MarkerOptions()
                .position(LatLng(track.end_latlng[1], track.end_latlng[0]))
                .title("도착점")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.goal_flag))
                .anchor(0F, 1F))

            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(track.start_latlng[1], track.start_latlng[0]), 18.0f)) // 화면 이동

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
                        Toast.makeText(this@TrackRecordActivity, "위치 권한이 없어 해당 기능을 이용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    // 시작 가능한 위치인지 확인
    private fun checkCanStartLocation() {
        inCanStartArea = mLocation.distanceTo(startPoint) < 20.0
        println("시작 가능 위치 내인지: $inCanStartArea")
        if (inCanStartArea) {
            binding.tvInformation.visibility = View.GONE
        } else {
            binding.tvInformation.visibility = View.VISIBLE
        }
    }

    // 레트로핏 초기화
    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java)
    }

    // 브로드캐스트를 받을 리시버
    inner class MBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            // flag 에 따라 분기처리
            when (intent?.getStringExtra("flag")) {
                TrackRecordService.LAST_LOCATION -> { // 마지막 위치
                    mLocation = intent?.getParcelableExtra(TrackRecordService.LOCATION)!!
                    println("마지막 위치 잘 받음 ${mLocation.latitude} ${mLocation.longitude}")
                    // 내 위치 마커 생성
                    mLocationMarker = mGoogleMap.addMarker(
                        MarkerOptions().position(LatLng(mLocation.latitude, mLocation.longitude)).icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.round_circle_black_24dp)))

                    binding.tvInformation.text = "시작 가능 위치로 이동하세요."
                    binding.tvInformation.setBackgroundColor(R.color.red)

                    checkCanStartLocation() // 시작 가능 위치인지 확인
                }

                TrackRecordService.BEFORE_START_LOCATION_UPDATE -> { // 시작 전 위치 업데이트
                    mLocation = intent?.getParcelableExtra(TrackRecordService.LOCATION)!!
                    println("리시버로 위치 받음 ${mLocation.latitude}, ${mLocation.longitude}")

                    mLocationMarker?.position = LatLng(mLocation.latitude, mLocation.longitude) // 마커 이동

                    checkCanStartLocation() // 시작 가능 위치인지 확인
                }

                TrackRecordService.IS_STARTED -> { // 시작 중인데 액티비티 재실행 시
                    started = true

                    // 버튼 바꿈
                    binding.startButton.visibility = View.GONE
                    binding.stopButton.visibility = View.VISIBLE

                    val second = intent?.getIntExtra(TrackRecordService.SECOND, 0)
                    binding.tvTime.text = Utils.timeToText(second)

                    val distance = intent?.getDoubleExtra(TrackRecordService.DISTANCE, 0.0)
                    binding.tvDistance.text = Utils.distanceToText(distance)

                    val avgSpeed = intent?.getDoubleExtra(TrackRecordService.AVG_SPEED, 0.0)
                    binding.tvAvgSpeed.text = Utils.avgSpeedToText(avgSpeed)

                    val locationList = intent?.getParcelableArrayListExtra<Location>(TrackRecordService.LOCATION_LIST)!!
                    println("arrayList 크기: ${locationList.size}")

                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationList[locationList.size-1].latitude, locationList[locationList.size-1].longitude), 18.0f)) // 화면 이동

                    // 마커 생성
                    mLocationMarker = mGoogleMap.addMarker(
                        MarkerOptions().position(LatLng(locationList[locationList.size-1].latitude, locationList[locationList.size-1].longitude)).icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.round_circle_black_24dp)))

                    // 선 그리기
                    beforeLocation = locationList[0]
                    for (i in 1 until locationList.size) {
                        mGoogleMap.addPolyline(PolylineOptions().add(LatLng(beforeLocation.latitude, beforeLocation.longitude), LatLng(locationList[i].latitude, locationList[i].longitude))) // 그림 그림
                        beforeLocation = locationList[i]
                    }

                    println("다 그림")
                }

                TrackRecordService.RECORD_START_LOCATION -> { // 기록 시작 위치
                    println("업데이트 시작 위치 받음")
                    val location = intent?.getParcelableExtra<Location>(TrackRecordService.LOCATION)!!
                    beforeLocation = location
                }

                TrackRecordService.AFTER_START_UPDATE -> { // 기록 시작 후 초마다 받는 업데이트
                    println("시작 후  업데이트 받음")

                    val second = intent?.getIntExtra(TrackRecordService.SECOND, 0)
                    binding.tvTime.text = Utils.timeToText(second)

                    val location = intent?.getParcelableExtra<Location>(TrackRecordService.LOCATION)!!

                    // 위치 다르면 관련 정보 수정하고 마커 이동하고 선 그림
                    if ((location.latitude != beforeLocation.latitude) || (location.longitude != beforeLocation.longitude)) {
                        val distance = intent?.getDoubleExtra(TrackRecordService.DISTANCE, 0.0)
                        binding.tvDistance.text = Utils.distanceToText(distance)

                        val avgSpeed = intent?.getDoubleExtra(TrackRecordService.AVG_SPEED, 0.0)
                        binding.tvAvgSpeed.text = Utils.avgSpeedToText(avgSpeed)

                        mLocationMarker?.position = LatLng(location.latitude, location.longitude) // 마커 이동
                        mGoogleMap.addPolyline(PolylineOptions().add(LatLng(beforeLocation.latitude, beforeLocation.longitude), LatLng(location.latitude, location.longitude))) // 그림 그림

                        beforeLocation = location
                    }
                }
            }
        }
    }
}