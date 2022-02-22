package com.example.capstonandroid.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.capstonandroid.R
import com.example.capstonandroid.RecordService
import com.example.capstonandroid.Utils
import com.example.capstonandroid.databinding.ActivityRecordBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task

const val LOCATION_PERMISSION_REQUEST_CODE = 100 // 위치 권한 요청 코드

class RecordActivity : AppCompatActivity(), OnMapReadyCallback {

    private var _binding: ActivityRecordBinding? = null
    private val binding: ActivityRecordBinding get() = _binding!!

    private lateinit var mGoogleMap: GoogleMap // 구글맵 선언

    private lateinit var updateTimer: Runnable // 타이머 업데이트

    private lateinit var mFusedLocationClient: FusedLocationProviderClient // 통합 위치 제공자 핸들

    private lateinit var mLocationCallback: LocationCallback // 위치 정보 업데이트 콜백

    private lateinit var mLocation: Location // 내 위치

    private lateinit var startLatLng: LatLng // 시작 위치

    private lateinit var endLatLng: LatLng // 끝 위치

    private val locationList = ArrayList<Location>() // 위치 리스트

    private val mHandler= Handler(Looper.getMainLooper()) // 타이머를 위한 핸들러

    private var mLocationMarker: Marker? = null // 내 위치 마커

    private var isStarted: Boolean = false // 기록 시작됐는지

    private var mService: RecordService? = null // 서비스

    private var mIsBound: Boolean? = null // 서비스 바인딩 됐는지 확인

    private var timeValue: Int = 0 // 시간 (초)

    private var altitude: Double = 0.0 // 현재 고도

    private var sumAltitude: Double = 0.0 // 누적 상승 고도

    private var distance = 0.0 // 거리

    private var avgSpeed = 0.0 // 평균 속도

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this) // 통합 위치 제공자 초기화

        mLocationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                var location = locationResult.lastLocation
                println("위도: ${location.latitude}, 경도: ${location.longitude}")
                mLocationMarker?.position = LatLng(location.latitude, location.longitude) // 마커 이동

                // 시작된 상태면
                if (isStarted) {

                    // 고도가 만약 더 크면 누적 상승 고도 더해줌
                    if (altitude < location.altitude) {
                        sumAltitude += location.altitude - altitude
                    }
                    altitude = location.altitude

                    // 폴리라인 그려줌
                    endLatLng = LatLng(location.latitude, location.longitude)
                    drawPolyline()
                    startLatLng = endLatLng

                    // 거리 구해줌
                    distance += mLocation.distanceTo(location)
                    binding.tvDistance.text = "%.2f".format(distance / 1000) // 소수점 둘째자리 수까지 반올림

                    //평균속도
                    if (timeValue > 0) {
                        avgSpeed = (distance / 1000) / (timeValue.toDouble() / 3600)
                        binding.tvAvgSpeed.text = "%.2f".format(avgSpeed)
                    }

                    locationList.add(location) //배열에 추가
                }
                mLocation = location
            }
        }

        // 타이머 업데이트 러너블
        updateTimer = object : Runnable {
            override fun run() {
                timeValue ++

                // 노티피케이션과 액티비티의 시간 업데이트
                binding.tvTime.text = Utils.timeToText(timeValue)
                mService?.timeValue = timeValue
                mService?.updateNotification()

                mHandler.postDelayed(this, 1000)
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    // 시작
    fun startRecord() {
        mHandler.postDelayed(updateTimer, 1000)
    }

    // 끝
    fun completeRecord() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("기록 완료")
            .setMessage("정말 기록을 종료 하시겠습니까?")
            .setNegativeButton("완료", DialogInterface.OnClickListener { _, _ ->
                isStarted = false
                mHandler.removeCallbacks(updateTimer) // 시간 업데이트 취소
                mFusedLocationClient.removeLocationUpdates(mLocationCallback) // 위치 업데이트 취소
                val intent = Intent(this@RecordActivity, CompleteRecordActivity::class.java)
                intent.putExtra("sumAltitude", sumAltitude)
                intent.putExtra("distance", distance)
                intent.putExtra("avgSpeed", avgSpeed)
                intent.putExtra("timeValue", timeValue)
                intent.putParcelableArrayListExtra("locationList", locationList)

                startActivity(intent)
                finish()
            })
            .setPositiveButton("계속 진행", DialogInterface.OnClickListener { _, _ ->

            })

            builder.show()
        // 기록 완료 화면으로 이동
    }

    override fun onStart() {
        super.onStart()
        println("onStart() 호출")
    }

    override fun onRestart() {
        super.onRestart()
        println("onRestart() 호출")
    }

    @SuppressLint("MissingPermission") // 권한설정하라고 오류표시뜨는거 없애 줌
    private fun startProcess() {
        println("프로세스 시작함.")

        bindService()
    }

    private fun bindService() {
        val intent  =Intent(this, RecordService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE) // 서비스 만들고 바인드 해 줌
    }

    private fun unbindService() {
        unbindService(serviceConnection)
    }

    // 맵이 준비 됐을 때 콜백
    override fun onMapReady(googleMap: GoogleMap) {
        println("onMapReady() 불림")
        mGoogleMap = googleMap
        println(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)
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
                println("위치 두개 옴")
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

    override fun onBackPressed() {
        println("onBackPressed 호출")

        // 달리기중 아닐떄만 뒤로 갈 수 있게 함
        if (!isStarted) {
//            unbindService()
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

        mFusedLocationClient.removeLocationUpdates(mLocationCallback) // 위치 엄데이트 제거
        _binding = null
    }

    override fun finish() {
        super.finish()

        println("finish 호출")
    }

    // 서비스 바인딩에 사용하는 커넥션
    private val serviceConnection = object : ServiceConnection {
        // 바인드 됐을 떄
        override fun onServiceConnected(className: ComponentName?, iBinder: IBinder?) {
            println("onServiceConnected() 호출")

            val binder = iBinder as RecordService.MBinder
            mService = binder.getService()
            mIsBound = true

            createLocationRequest()

            // 시작 버튼 눌렀을 때
            binding.startButton.setOnClickListener {
                if (!isStarted) {
                    startLatLng = LatLng(mLocation.latitude, mLocation.longitude)
                    altitude = mLocation.altitude
                    locationList.add(mLocation)
                    startRecord()
                    isStarted = true
                    println("시작 버튼 클릭")
                    binding.startButton.text = "종료"
                } else { // 종료 버튼을 눌렀을 때
                    println("종료 버튼 누름")
                    completeRecord()
                }
            }

            binding.startButton.isEnabled = true
        }

        // 바인드 끊겼을떄
        override fun onServiceDisconnected(className: ComponentName?) {
            println("onServiceDisconnected() 호출")
            mIsBound = false
        }

    }

    // 맵에 선 그림
    private fun drawPolyline() {
        mGoogleMap.addPolyline(PolylineOptions().add(startLatLng, endLatLng))
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
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(mLocation.latitude, mLocation.longitude), 18.0f)) // 화면 이동

                // 내 위치 마커 생성
                mLocationMarker = mGoogleMap.addMarker(MarkerOptions().position(LatLng(mLocation.latitude, mLocation.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.round_circle_black_24dp)))
            }
        }

        // 위치 요청 생성
        val locationRequest = LocationRequest.create()?.apply {
            interval = 2000 // 간격
            fastestInterval = 1000 // 최대 간격 ( 다른 앱에서 위치정보 수집해도, 여기서도 받아지는 듯 함? )
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
}