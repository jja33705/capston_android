package com.example.capstonandroid.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityRecordBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task

const val LOCATION_PERMISSION_REQUEST_CODE = 100 // 위치 권한 요청 코드
const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 200 // 백그라운드 위치 권한 요청 코드

class RecordActivity : AppCompatActivity(), OnMapReadyCallback {

    private var _binding: ActivityRecordBinding? = null
    private val binding: ActivityRecordBinding get() = _binding!!

    private lateinit var googleMap: GoogleMap // 구글맵 선언

    private lateinit var fusedLocationClient: FusedLocationProviderClient // 통합 위치 제공자 핸들

    private lateinit var myLocation: Location // 내 위치

    private lateinit var locationCallback: LocationCallback // 위치 정보 업데이트 콜백

    private var myLocationMarker: Marker? = null // 내 위치 마커

    private val startTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 업데이트된 위치정보 받아오는 콜백
        locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                println("위치 개수: ${locationResult.locations.size}, 시간: ${System.currentTimeMillis() - startTime}")
                for (location in locationResult.locations) {
                    myLocation = location
                    println("고도: ${myLocation.altitude}")
                    binding.tvLat.text = "" + myLocation.latitude
                    binding.tvLng.text = "" + myLocation.longitude
                    myLocationMarker?.position = LatLng(myLocation.latitude, myLocation.longitude)
                }
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    @SuppressLint("MissingPermission") // 권한설정하라고 오류표시뜨는거 없애 줌
    private fun startProcess() {
        println("프로세스 시작함.")

        // 통합 위치 정보 제공자 핸들 가져옴
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 내 마지막 위치를 가져옴.
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            // 내 마지막 위치 못 가져왔을 때
            if (location == null) {
                println("마지막 위치 못가져옴")
            } else {
                println("마지막 위치 잘 가져옴")
                myLocation = location
                binding.tvLat.text = "" + myLocation.latitude
                binding.tvLng.text = "" + myLocation.longitude
                println("고도: ${myLocation.altitude}")
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(myLocation.latitude, myLocation.longitude), 18.0f))
//                googleMap.isMyLocationEnabled = true

                // 내 위치 마커 생성
                myLocationMarker = googleMap.addMarker(
                    MarkerOptions().position(LatLng(myLocation.latitude, myLocation.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.round_circle_black_24dp))
                )

                createLocationRequest()
            }
        }

        // 시작 버튼 눌렀을 때
        binding.startButton.setOnClickListener {
            println("시작 버튼 클릭")
        }
    }

    @SuppressLint("MissingPermission")
    private fun createLocationRequest() {

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
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
        task.addOnFailureListener {
            println("location client 설정 실패")
        }
    }

    // 맵이 준비 됐을 때 콜백
    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap
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
            // 백그라운드 거부한 상태면 true 반환
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) -> {
                println("거부함")
                // 처음 들어와서 위치 권한이 없는 거면
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE)
                }
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
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE)
                    } else {
                        Toast.makeText(this@RecordActivity, "위치 권한이 없어 해당 기능을 이용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                println("백그라운드 옴")
                println("백그라운드 위치 권한 요청 응답: ${grantResults[0] == PackageManager.PERMISSION_GRANTED}")
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this@RecordActivity, "백그라운드 위치 권한이 없어 해당 기능을 이용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    startProcess()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
//        moveTaskToBack(true)
        println("onBackPressed 호출")
    }

    override fun onStop() {
        super.onStop()

        println("onStop 호출")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

        println("onDestroy 호출")
    }

    override fun finish() {
        //super.finish()

        println("finish 호출")
    }
}