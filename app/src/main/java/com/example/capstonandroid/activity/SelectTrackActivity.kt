package com.example.capstonandroid.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivitySelectTrackBinding
import com.example.capstonandroid.network.dto.Track
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.*
import retrofit2.Retrofit

class SelectTrackActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolylineClickListener,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnMapClickListener {

    private var _binding: ActivitySelectTrackBinding? = null
    private val binding get() = _binding!!

    private lateinit var mGoogleMap: GoogleMap

    private lateinit var retrofit: Retrofit // 레트로핏 인스턴스
    private lateinit var supplementService: BackendApi // api

    private lateinit var mFusedLocationClient: FusedLocationProviderClient // 통합 위치 제공자 핸들

    private lateinit var mLocationCallback: LocationCallback // 위치 정보 업데이트 콜백

    private lateinit var mLocation: Location // 내 위치

    private lateinit var job: Job // 코루틴 동작을 제어하기 위한 job

    private lateinit var tracks: ArrayList<Track> // 트랙 리스트

    private lateinit var polylineList: ArrayList<Polyline>

    private lateinit var markerList: ArrayList<Marker>

    private lateinit var exerciseKind: String // 운동 종류

    private lateinit var matchType: String // 매치 타입

    private lateinit var trackMarker: View // 커스텀 마커 뷰

    private lateinit var trackMarkerTextView: TextView // 커스텀 마커 텍스트 뷰

    private var mLocationMarker: Marker? = null // 내 위치 마커

    private var updateTrack = false // 트랙을 새로 불러올지 말지를 결정하는 불린 변수
    private var updatingTrack = false // 트랙 정보를 업데이트 중인지를 알려주는 불린 함수

    private var selectedTrackIndex: Int? = null // 선택된 트랙 인덱스

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivitySelectTrackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        trackMarker = LayoutInflater.from(this).inflate(R.layout.track_and_name_marker, null)!! // 마커 레이아웃 파일 불러옴
        trackMarkerTextView = trackMarker.findViewById(R.id.tv_marker) as TextView

        supportActionBar?.title = "트랙 선택" // 액션바 텍스트 수정

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this) // 통합 위치 제공자 초기화

        // 위치 요청 응답 왔을 때 콜백
        mLocationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                mLocation = locationResult.lastLocation
            }
        }

        // intent 로 받아온 변수 초기화
        val intent = intent
        exerciseKind = intent.getStringExtra("exerciseKind")!!
        matchType = intent.getStringExtra("matchType")!!

        job = Job() // job 생성

        tracks = ArrayList<Track>()

        polylineList = ArrayList<Polyline>()

        markerList = ArrayList<Marker>()

        initRetrofit() // retrofit 인스턴스 초기화

        // 시작 버튼 초기화
        binding.btnStart.setOnClickListener {
            val intent = Intent(this@SelectTrackActivity, TrackRecordActivity::class.java)
            intent.putExtra("exerciseKind", exerciseKind)
            intent.putExtra("matchType", matchType)
            intent.putExtra("trackId", tracks[selectedTrackIndex!!]._id)

            startActivity(intent)
            finish()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // 보이는 지도에 맞게 트랙 가져와 지도에 그려 줌
    private fun initTracks(bounds: List<Double>) {
        if (updatingTrack) {
            println("이미 업데이트 중")
            return
        }

        CoroutineScope(Dispatchers.Main + job).launch {
            println("실행됨")
            updatingTrack = true

            println(tracks.size)
            println(polylineList.size)
            println(markerList.size)

            // 현재 있는 폴리라인, 마커 지움
            for (i in 0 until tracks.size) {
                polylineList[i].remove()
                markerList[i].remove()
            }
            polylineList.clear()
            markerList.clear()
            println("다 지움")

            // track 리스트 가져오는 api 호출
            val tracksResponse = supplementService.getTracks("http://13.124.24.179/api/track/search", bounds, 16, "B")

            if (tracksResponse.isSuccessful) {
                // 1개 이상 응답이 오면 데이터 보관하고 맵에 그린다.
                println("응답 옴 ${tracksResponse.body()}")
                tracks = tracksResponse.body()!!.result
                for ((index, track) in tracks.withIndex()) {

                // 마커 생성하고 마커 리스트에 넣음
                    trackMarkerTextView.text = track.trackName
                    val marker = mGoogleMap.addMarker(MarkerOptions()
                        .position(LatLng(track.start_latlng[1], track.start_latlng[0]))
                        .title(track.trackName)
                        .icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView()))
                        .anchor(0.1F, 1F))
                    marker?.tag = index

                    markerList.add(marker!!)

                    // 폴리라인 그리고 폴리라인 리스트에 넣음
                    val latLngList = ArrayList<LatLng>()

                    for (coordinate in track.gps.coordinates) {
                        latLngList.add(LatLng(coordinate[1], coordinate[0]))
                        println("${coordinate[1]}, ${coordinate[0]}")
                    }

                    val polyline = mGoogleMap.addPolyline(PolylineOptions()
                        .clickable(true)
                        .addAll(latLngList)
                        .width(10F)
                        .color(R.color.main_color))
                    polyline.tag = index

                    polylineList.add(polyline)
                    println("$index 끝남")
                }
            }

            println("다 그림")
            updatingTrack = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // job 취소
        mFusedLocationClient.removeLocationUpdates(mLocationCallback) // 위치 업데이트 제거
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        // 폴리라인 클릭 리스너 등록
        mGoogleMap.setOnPolylineClickListener(this)

        // 마커 클릭 리스너 등록
        mGoogleMap.setOnMarkerClickListener(this)

        // 맵 클릭 리스너 등록
        mGoogleMap.setOnMapClickListener(this)

        // 카메라 움직임이 시작 됐을 때 리스너
        mGoogleMap.setOnCameraMoveStartedListener(this)

        // 카메라 이동 끝났을때 콜백 리스너
        mGoogleMap.setOnCameraIdleListener(this)

        checkPermission()
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

                // 내 위치 마커 생성
                mLocationMarker = mGoogleMap.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.round_circle_black_24dp)))

                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 11.0f)) // 화면 이동
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

    // 레트로핏 초기화
    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java)
    }

    // 폴리라인이나 마커를 클릭해서 선택했을 때
    private fun selectTrack(newSelectedIndex: Int) {
        println("selectTrack 호출 $newSelectedIndex")

        if (selectedTrackIndex == null) {
            for (i in 0 until tracks.size) {
                if (i != newSelectedIndex) {
                    markerList[i].alpha = 0.3F
                    polylineList[i].color = R.color.selected_polyline_color
                }
            }
            selectedTrackIndex = newSelectedIndex

            binding.slidingLayout.panelHeight = 1000 // 하단 바 올려줌
        } else {
            markerList[selectedTrackIndex!!].alpha = 0.3F
            polylineList[selectedTrackIndex!!].color = R.color.selected_polyline_color

            markerList[newSelectedIndex].alpha = 1F
            polylineList[newSelectedIndex].color = R.color.main_color

            selectedTrackIndex = newSelectedIndex
        }

        binding.trackTitle.text = tracks[selectedTrackIndex!!].trackName
        binding.trackDescription.text = tracks[selectedTrackIndex!!].description
        binding.trackDistance.text = "${tracks[selectedTrackIndex!!].totalDistance}km"
    }

    // 폴리라인 클릭 이벤트 처리
    override fun onPolylineClick(polyline: Polyline) {
        selectTrack(polyline.tag as Int)
    }

    // 권한 확인
    private fun checkPermission() {
        when {
            // 위치 권한 다 허용돼 있으면 시작
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                createLocationRequest()
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
                        createLocationRequest()
                    } else {
                        Toast.makeText(this@SelectTrackActivity, "위치 권한이 없어 해당 기능을 이용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    // 카메라 이동 시작했을 때 리스너
    override fun onCameraMoveStarted(reason: Int) {
        when(reason) { // 어떻게 시작된 움직임인지 알 수 있다.
            GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION -> { // 사용자 작업에 대한 응답으로 시작(마커 클릭 시 등..)
                println("move reason: REASON_API_ANIMATION")
                updateTrack = false
            }
            GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION -> { // 개발자가 프로그래밍적으로 시작
                println("move reason: REASON_DEVELOPER_ANIMATION")
                updateTrack = true
            }
            GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> { // 사용자 제스처
                println("move reason: REASON_GESTURE")

                // 선택된 트랙이 있으면 새로 불러오지 말고 없으면 새로 불러옴
                updateTrack = selectedTrackIndex == null
            }
        }
    }

    // 카메라 이동 끝났을때 리스너
    override fun onCameraIdle() {
        println("camera idle")
        if (updateTrack) {
            val latLngBounds = mGoogleMap.projection.visibleRegion.latLngBounds

            println("위치 경계: ${latLngBounds.southwest.longitude} ${latLngBounds.southwest.latitude} ${latLngBounds.northeast.longitude} ${latLngBounds.northeast.latitude}")
            initTracks(listOf(latLngBounds.southwest.longitude, latLngBounds.southwest.latitude, latLngBounds.northeast.longitude, latLngBounds.northeast.latitude))

            selectedTrackIndex = null
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        selectTrack(marker.tag as Int)
        return true // 기본 이벤트 발동 안함
    }

    override fun onMapClick(latLng: LatLng) {
        println("onMapClick 호출")

        // 선택돼 있는 트랙이 있으면 취소함
        if (selectedTrackIndex != null) {
            binding.slidingLayout.panelHeight = 0 // 하단 패널 내림

            for (i in 0 until tracks.size) {
                if (i != selectedTrackIndex) {
                    markerList[i].alpha = 1F
                    polylineList[i].color = R.color.main_color
                }
            }

            selectedTrackIndex = null
        }
    }

    private fun createBitmapFromView(): Bitmap {
        trackMarker.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        trackMarker.layout(0, 0, trackMarker.measuredWidth, trackMarker.measuredHeight)

        val bitmap = Bitmap.createBitmap(trackMarker.measuredWidth,
            trackMarker.measuredHeight,
            Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)

        trackMarker.background?.draw(canvas)
        trackMarker.draw(canvas)

        return bitmap
    }
}