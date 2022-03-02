package com.example.capstonandroid.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.capstonandroid.R
import com.example.capstonandroid.RecordService
import com.example.capstonandroid.Utils
import com.example.capstonandroid.databinding.ActivityRecordBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

const val LOCATION_PERMISSION_REQUEST_CODE = 100 // 위치 권한 요청 코드

class RecordActivity : AppCompatActivity(), OnMapReadyCallback {

    private var _binding: ActivityRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var mGoogleMap: GoogleMap // 구글맵 선언

    private lateinit var kind: String // 운동 종류

    private lateinit var mBroadcastReceiver: MBroadcastReceiver // 브로드캐스트 리시버

    private lateinit var beforeLocation: Location // 선 긋기 시작 위치

    private var mLocationMarker: Marker? = null // 내 위치 마커

    private var isStarted: Boolean = false // 기록 시작됐는지

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent: Intent = intent
        kind = intent.getStringExtra("kind").toString()
        println(kind)

        // 시작 버튼 초기화
        binding.startButton.setOnClickListener{
            if (!isStarted) { // 아직 시작 안했을 때
                println("버튼 클릭함")
                // 커맨드 보냄 (서비스는 한번 더 실행 안되니 커맨드가 보내진다.)
                val intent = Intent(this@RecordActivity, RecordService::class.java)
                intent.action = RecordService.START_RECORD
                startForegroundService(intent)

                isStarted = true
                binding.startButton.setBackgroundResource(R.drawable.stop_button)
            } else { // 시작 중일 때
                val intent = Intent(this@RecordActivity, RecordService::class.java)
                intent.action = RecordService.COMPLETE_RECORD
                startForegroundService(intent)

                finish()
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onStart() {
        super.onStart()
        println("onStart() 호출")
    }

    override fun onRestart() {
        super.onRestart()
        println("onRestart() 호출")
    }

    @SuppressLint("NewApi") // 권한설정하라고 오류표시뜨는거 없애 줌
    private fun startProcess() {
        println("프로세스 시작함.")

        // 브로드캐스트 설정
        mBroadcastReceiver = MBroadcastReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, IntentFilter(RecordService.ACTION_BROADCAST))

        // 서비스 시작
        val intent: Intent = Intent(this@RecordActivity, RecordService::class.java)
        intent.action = RecordService.START_PROCESS
        startForegroundService(intent)
    }

    // 맵이 준비 됐을 때 콜백
    override fun onMapReady(googleMap: GoogleMap) {
        println("onMapReady() 불림")
        mGoogleMap = googleMap
        checkPermission()
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
                        Toast.makeText(this@RecordActivity, "위치 권한이 없어 해당 기능을 이용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    @SuppressLint("NewApi")
    override fun onBackPressed() {
        println("onBackPressed 호출")

        // 달리기중 아닐떄만 뒤로 갈 수 있게 함
        if (!isStarted) {
            // 서비스 종료하라고 커맨드 보냄
            val intent = Intent(this@RecordActivity, RecordService::class.java)
            intent.action = RecordService.STOP_SERVICE
            startForegroundService(intent)
            super.onBackPressed()
        }
    }

    override fun onStop() {
        super.onStop()

        println("onStop 호출")
    }

    override fun onDestroy() {
        println("onDestroy 호출")
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver)
        _binding = null
    }

    override fun finish() {
        super.finish()

        println("finish 호출")
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
                    mLocationMarker = mGoogleMap.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.round_circle_black_24dp)))

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
                    mLocationMarker = mGoogleMap.addMarker(MarkerOptions().position(LatLng(locationList[locationList.size-1].latitude, locationList[locationList.size-1].longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.round_circle_black_24dp)))

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