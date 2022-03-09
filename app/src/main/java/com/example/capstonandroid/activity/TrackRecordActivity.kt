package com.example.capstonandroid.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.capstonandroid.R
import com.example.capstonandroid.RecordService
import com.example.capstonandroid.Utils
import com.example.capstonandroid.databinding.ActivityTrackRecordBinding
import com.example.capstonandroid.dto.Track
import com.example.capstonandroid.network.BackendApi
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

    private lateinit var mBroadcastReceiver: MBroadcastReceiver // 브로드캐스트 리시버

    private lateinit var beforeLocation: Location // 선 긋기 시작 위치

    private var mLocationMarker: Marker? = null // 내 위치 마커

    private var isStarted: Boolean = false // 기록 시작됐는지

    private lateinit var job: Job // 코루틴 동작을 제어하기 위한 job

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

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun startProcess() {
        initTrack()

        mBroadcastReceiver = MBroadcastReceiver()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        checkPermission()
    }

    // 트랙 정보 가져와서 그림
    private fun initTrack() {
        CoroutineScope(Dispatchers.Main + job).launch {
            val trackResponse = supplementService.getTrack("http://13.124.24.179/api/track/${trackId}")
            println("응답코드: ${trackResponse.code()}")

            if (trackResponse.isSuccessful) {
                println("응답: ${trackResponse.body()}")
                track = trackResponse.body()!!

                val latLngList = ArrayList<LatLng>()

                for (coordinate in track.gps.coordinates) {
                    latLngList.add(LatLng(coordinate[1], coordinate[0]))
                    println("${coordinate[1]}, ${coordinate[0]}")
                }

                val polyline = mGoogleMap.addPolyline(PolylineOptions()
                    .clickable(true)
                    .addAll(latLngList))
                polyline.width = 10F
                polyline.color = Color.RED
            }
            println("다 그림")
        }
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
                RecordService.LAST_LOCATION -> { // 마지막 위치
                    val location = intent?.getParcelableExtra<Location>(RecordService.LOCATION)!!
                    println("리시버로 마지막 위치 받음 ${location.latitude}, ${location.longitude}")

                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 18.0f)) // 화면 이동

                    // 내 위치 마커 생성
                    mLocationMarker = mGoogleMap.addMarker(
                        MarkerOptions().position(LatLng(location.latitude, location.longitude)).icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.round_circle_black_24dp)))

                    binding.startButton.isEnabled = true
                }
                RecordService.BEFORE_START_LOCATION_UPDATE -> { // 시작 전 위치 업데이트
                    val location = intent?.getParcelableExtra<Location>(RecordService.LOCATION)!!
                    println("리시버로 위치 받음 ${location.latitude}, ${location.longitude}")

                    mLocationMarker?.position = LatLng(location.latitude, location.longitude) // 마커 이동
                }
                RecordService.IS_STARTED -> { // 시작 중인데 액티비티 재실행 시
                    isStarted = true

                    val second = intent?.getIntExtra(RecordService.SECOND, 0)
                    binding.tvTime.text = Utils.timeToText(second)

                    val distance = intent?.getDoubleExtra(RecordService.DISTANCE, 0.0)
                    binding.tvDistance.text = Utils.distanceToText(distance)

                    val avgSpeed = intent?.getDoubleExtra(RecordService.AVG_SPEED, 0.0)
                    binding.tvAvgSpeed.text = Utils.avgSpeedToText(avgSpeed)

                    val locationList = intent?.getParcelableArrayListExtra<Location>(RecordService.LOCATION_LIST)!!
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

                    binding.startButton.setBackgroundResource(R.drawable.stop_button)
                    binding.startButton.isEnabled = true
                }
                RecordService.RECORD_START_LOCATION -> { // 기록 시작 위치
                    println("업데이트 시작 위치 받음")
                    val location = intent?.getParcelableExtra<Location>(RecordService.LOCATION)!!
                    beforeLocation = location
                }
                RecordService.AFTER_START_UPDATE -> { // 기록 시작 후 초마다 받는 업데이트
                    println("시작 후  업데이트 받음")

                    val second = intent?.getIntExtra(RecordService.SECOND, 0)
                    binding.tvTime.text = Utils.timeToText(second)

                    val location = intent?.getParcelableExtra<Location>(RecordService.LOCATION)!!

                    // 위치 다르면 관련 정보 수정하고 마커 이동하고 선 그림
                    if ((location.latitude != beforeLocation.latitude) || (location.longitude != beforeLocation.longitude)) {
                        val distance = intent?.getDoubleExtra(RecordService.DISTANCE, 0.0)
                        binding.tvDistance.text = Utils.distanceToText(distance)

                        val avgSpeed = intent?.getDoubleExtra(RecordService.AVG_SPEED, 0.0)
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