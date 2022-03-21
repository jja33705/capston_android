package com.example.capstonandroid.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.capstonandroid.R
import com.example.capstonandroid.TrackClusterRenderer
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
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.coroutines.*
import retrofit2.Retrofit

class SelectTrackActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private var _binding: ActivitySelectTrackBinding? = null
    private val binding get() = _binding!!

    private lateinit var mGoogleMap: GoogleMap

    private lateinit var retrofit: Retrofit // 레트로핏 인스턴스
    private lateinit var supplementService: BackendApi // api

    private lateinit var mFusedLocationClient: FusedLocationProviderClient // 통합 위치 제공자 핸들

    private lateinit var mLocationCallback: LocationCallback // 위치 정보 업데이트 콜백

    private lateinit var mLocation: Location // 내 위치

    private lateinit var job: Job // 코루틴 동작을 제어하기 위한 job

    private lateinit var trackMap: HashMap<String, Track> // 트랙 맵

    private lateinit var exerciseKind: String // 운동 종류

    private lateinit var matchType: String // 매치 타입

    private lateinit var clusterManager: ClusterManager<TrackItem> // 클러스터 매니저

    private var mLocationMarker: Marker? = null // 내 위치 마커

    private var selectedTrackId: String? = null // 선택된 트랙 인덱스

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivitySelectTrackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "트랙 선택" // 액션바 텍스트 수정

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this) // 통합 위치 제공자 초기화

        // 위치 요청 응답 왔을 때 콜백
        mLocationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                mLocation = locationResult.lastLocation
                mLocationMarker?.position = LatLng(mLocation.latitude, mLocation.longitude) // 마커 이동
            }
        }

        // intent 로 받아온 변수 초기화
        val intent = intent
        exerciseKind = intent.getStringExtra("exerciseKind")!!
        matchType = intent.getStringExtra("matchType")!!

        job = Job() // job 생성

        trackMap = HashMap()

        initRetrofit() // retrofit 인스턴스 초기화

        // 시작 버튼 초기화
        binding.btnStart.setOnClickListener {
            val intent = Intent(this@SelectTrackActivity, TrackRecordActivity::class.java)
            intent.putExtra("exerciseKind", exerciseKind)
            intent.putExtra("matchType", matchType)
            intent.putExtra("trackId", selectedTrackId)

            startActivity(intent)
            finish()
        }

        // 현재 위치에서 검색 버튼 초기화
        binding.buttonSearchTrack.setOnClickListener {
            initTracks()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // 보이는 지도에 맞게 트랙 가져와 지도에 그려 줌
    private fun initTracks() {
        CoroutineScope(Dispatchers.Main + job).launch {
            println("실행됨")

            // 보고있는 카메라 위치 측정
            val latLngBounds = mGoogleMap.projection.visibleRegion.latLngBounds
            println("위치 경계: ${latLngBounds.southwest.longitude} ${latLngBounds.southwest.latitude} ${latLngBounds.northeast.longitude} ${latLngBounds.northeast.latitude}")
            val bounds = listOf(latLngBounds.southwest.longitude, latLngBounds.southwest.latitude, latLngBounds.northeast.longitude, latLngBounds.northeast.latitude)

            // 현재 지도상에 보이는 트랙 가져오는 api 호출
            val tracksResponse = supplementService.getTracks("http://13.124.24.179/api/track/search", bounds, 16, "B")
            println("응답 옴 ${tracksResponse.body()}")
            if (tracksResponse.isSuccessful) {

                // 1개 이상 응답이 오면 맵에 데이터 보관하고 구글맵에 그린다.
                for (track in tracksResponse.body()!!.result) {

                    if (!trackMap.containsKey(track._id)) { // 현재 없는 트랙이면 추가함
                        trackMap[track._id] = track

                        val trackItem = TrackItem(LatLng(track.start_latlng[1], track.start_latlng[0]), track.trackName, track._id)
                        clusterManager.addItem(trackItem)

                        // 폴리라인 그리고 폴리라인 맵에 넣음
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
                        polyline.tag = track._id
                    }
                }
            }
            clusterManager.cluster() // 강제로 리 클러스터링 해 줘야 이미지가 렌더링 됨....
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

        // 맵 클릭 리스너 등록
        mGoogleMap.setOnMapClickListener(this)

        // 클러스터 리스너 등록
        clusterManager = ClusterManager(this, mGoogleMap)
        clusterManager.renderer = TrackClusterRenderer(this, mGoogleMap, clusterManager) // 마커 이미지 변경을 위한 것.
        mGoogleMap.setOnCameraIdleListener(clusterManager)
        mGoogleMap.setOnMarkerClickListener(clusterManager)

        // 클러스터 아이템 클릭했을 때 리스너
        clusterManager.setOnClusterItemClickListener { trackItem ->
            println("클릭된거 타이틀: ${trackItem.title}")
            println("클릭된거 아이디: ${trackItem.snippet}")

            clusterManager.markerCollection.markers.forEach { marker ->

            }

            true // 마커 클릭 기본 이벤트 발동 안하게 함
        }

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
                mLocationMarker = mGoogleMap.addMarker(MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.round_circle_black_24dp)))
                mLocationMarker?.tag = "myLocation"

                // 화면 이동하고 화면 이동 끝났을 때 맵 불러오는 콜백
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 11.0f), object : GoogleMap.CancelableCallback {
                    override fun onCancel() {

                    }

                    override fun onFinish() {
                        initTracks()
                    }

                } )
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

    override fun onMapClick(latLng: LatLng) {
        println("onMapClick 호출")

        // 선택돼 있는 트랙이 있으면 취소함
        if (selectedTrackId != null) {
            selectedTrackId = null

            binding.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        }
    }

    // 클러스팅하기 위한 마커 아이템
    inner class TrackItem(position: LatLng, title: String, snippet: String) : ClusterItem {

        private val position: LatLng = position
        private val title: String = title
        private val snippet: String = snippet

        override fun getPosition(): LatLng {
            return position
        }

        override fun getTitle(): String? {
            return title
        }

        override fun getSnippet(): String? {
            return snippet
        }

    }

}