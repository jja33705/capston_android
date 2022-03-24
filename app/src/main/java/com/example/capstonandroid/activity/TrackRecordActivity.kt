package com.example.capstonandroid.activity



import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.Room
import com.example.capstonandroid.R
import com.example.capstonandroid.RecordService
import com.example.capstonandroid.TrackRecordService
import com.example.capstonandroid.Utils
import com.example.capstonandroid.databinding.ActivityTrackRecordBinding
import com.example.capstonandroid.db.AppDatabase
import com.example.capstonandroid.db.dao.GpsDataDao
import com.example.capstonandroid.network.dto.Track
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.*
import retrofit2.Retrofit

class TrackRecordActivity : AppCompatActivity(), OnMapReadyCallback {
    private var _binding: ActivityTrackRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var exerciseKind: String
    private lateinit var trackId: String

    private lateinit var retrofit: Retrofit // 레트로핏 인스턴스
    private lateinit var supplementService: BackendApi // api

    private lateinit var mGoogleMap: GoogleMap

    private lateinit var gpsDataDao: GpsDataDao // db dao 핸들

    private lateinit var track: Track

    private lateinit var startPoint: Location // 시작점

    private lateinit var endPoint: Location // 끝점

    private lateinit var mBroadcastReceiver: MBroadcastReceiver // 브로드캐스트 리시버

    private lateinit var beforeLatLng: LatLng // 선 긋기 시작 위치

    private lateinit var canStartAreaCircle: Circle // 시작 가능한 반경 원

    private lateinit var job: Job // 코루틴 동작을 제어하기 위한 job

    private lateinit var checkpointList: ArrayList<Location> // 체크포인트 리스트

    private var mLocationMarker: Marker? = null // 내 위치 마커

    private var inCanStartArea = false // 시작 가능한 범위 내에 있는지

    private var gotFirstLocation = false // 첫 번째 위치를 받아와 시작 가능한 상태인지

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityTrackRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트로 넘어온 옵션값 받음
        val intent = intent
        exerciseKind = intent.getStringExtra("exerciseKind")!!
        trackId = intent.getStringExtra("trackId")!!
        println("exerciseKind $exerciseKind")
        println("trackId $trackId")

        // db 사용 설정
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database").build()
        gpsDataDao = db.gpsDataDao()

        job = Job() // job 생성

        initRetrofit()

        // 액티비티 이동 후 답을 받는 콜백
        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // 정상적으로 카운트다운 다 지나오면 시작
            if (result.resultCode == CountDownActivity.COUNT_DOWN_ACTIVITY_RESULT_CODE) {
                // 시작한 상태 저장
                getSharedPreferences("trackRecord", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isStarted", true)
                    .commit()

                // 커맨드 보냄 (서비스는 한번 더 실행 안되니 커맨드가 보내진다.)
                val intent = Intent(this@TrackRecordActivity, TrackRecordService::class.java)
                intent.action = TrackRecordService.START_RECORD
                startForegroundService(intent)

                // 버튼 바꿈
                binding.startButton.visibility = View.GONE
                binding.stopButton.visibility = View.VISIBLE

                // 시작 영역 없애고 도착 영역 그림
                canStartAreaCircle.remove()
                mGoogleMap.addCircle(CircleOptions()
                    .center(LatLng(endPoint.latitude, endPoint.longitude))
                    .radius(20.0)
                    .fillColor(R.color.area_color)
                    .strokeWidth(0F))
            }
        }

        // 시작 버튼 초기화
        binding.startButton.setOnClickListener{
            println("시작 버튼 클릭함")

            if (inCanStartArea) {
                val intent = Intent(this, CountDownActivity::class.java)
                activityResultLauncher.launch(intent)
            } else {
                Toast.makeText(this@TrackRecordActivity, "시작 가능 위치가 아닙니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 종료 버튼 초기화
        binding.stopButton.setOnClickListener {
            println("종료 버튼 클릭함")
            AlertDialog.Builder(this)
                .setTitle("기록 종료")
                .setMessage("정말로 기록을 종료하시겠습니까")
                .setPositiveButton("취소", DialogInterface.OnClickListener { _, _ ->
                })
                .setNegativeButton("종료", DialogInterface.OnClickListener { _, _ ->
                    // 기록 종료하는 경우
                    getSharedPreferences("trackRecord", MODE_PRIVATE).edit().putBoolean("isStarted", false)
                    onBackPressed()
                })
                .show()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("NewApi")
    override fun onBackPressed() {
        println("onBackPressed 호출")

        // 달리기중 아닐때만 뒤로 갈 수 있게 함
        if (!getSharedPreferences("trackRecord", MODE_PRIVATE).getBoolean("isStarted", false)) {
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
            launch {
                initTrack()
            }.join() // 트랙 초기화하는거 기다리고 다음 작업 수행함

            // 상태 저장
            getSharedPreferences("trackRecord", MODE_PRIVATE)
                .edit()
                .putString("exerciseKind", exerciseKind)
                .putString("matchType", "혼자하기")
                .putString("trackName", track.trackName)
                .putString("trackId", trackId)
                .commit()

            binding.tvInformation.setBackgroundColor(resources.getColor(R.color.green))
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

            //출발점 초기화
            startPoint = Location("startPoint")
            startPoint.latitude = track.start_latlng[1]
            startPoint.longitude = track.start_latlng[0]

            // 도착점 초기화
            endPoint = Location("endPoint")
            endPoint.latitude = track.end_latlng[1]
            endPoint.longitude = track.end_latlng[0]

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

            // 체크포인트 추가
            println("체크포인트")
            println(track.checkPoint)

            checkpointList = ArrayList()
            for (i in track.checkPoint.indices) {
                val location = Location("checkpoint")
                location.latitude = track.checkPoint[i][1]
                location.longitude = track.checkPoint[i][0]
                checkpointList.add(location)

                mGoogleMap.addMarker(MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .title("체크포인트 ${i + 1}")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.checkpoint_before))
                    .anchor(0.5F, 0.5F))
            }

            // 출발점 마커 추가
            mGoogleMap.addMarker(MarkerOptions()
                .position(LatLng(startPoint.latitude, startPoint.longitude))
                .title("출발점")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_marker))
                .anchor(0.5F, 1F))

            // 시작 가능 반경 그림
            canStartAreaCircle = mGoogleMap.addCircle(CircleOptions()
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
                TrackRecordService.BEFORE_START_LOCATION_UPDATE -> { // 시작 전 위치 업데이트
                    val latLng = intent?.getParcelableExtra<LatLng>(TrackRecordService.LAT_LNG)!!
                    println("리시버로 위치 받음 ${latLng.latitude}, ${latLng.longitude}")

                    if (!gotFirstLocation) {
                        gotFirstLocation = true

                        // 내 위치 마커 생성
                        mLocationMarker = mGoogleMap.addMarker(MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.round_circle_black_24dp)))

                        binding.tvInformation.text = "시작 가능 위치로 이동하세요."
                        binding.tvInformation.setBackgroundColor(resources.getColor(R.color.red))
                    } else {
                        mLocationMarker?.position = latLng // 마커 이동
                    }

                    // 시작 가능 위치인지 확인
                    val mLocation = Location("myLocation")
                    mLocation.latitude = latLng.latitude
                    mLocation.longitude = latLng.longitude

                    inCanStartArea = mLocation.distanceTo(startPoint) < 20.0
                    println("시작 가능 위치 내인지: $inCanStartArea")
                    if (inCanStartArea) {
                        binding.tvInformation.visibility = View.GONE
                    } else {
                        binding.tvInformation.visibility = View.VISIBLE
                    }
                }

                TrackRecordService.IS_STARTED -> { // 시작 중인데 액티비티 재실행 시
                    // lateinit 오류 발생하지 않게 바로 이전 위치부터 등록해줌
                    beforeLatLng = intent?.getParcelableExtra(RecordService.LAT_LNG)!!

                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(beforeLatLng, 18.0f)) // 화면 이동

                    // 마커 생성
                    mLocationMarker = mGoogleMap.addMarker(MarkerOptions()
                        .position(beforeLatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.round_circle_black_24dp)))

                    binding.tvInformation.visibility = View.GONE // 정보 창 없앰

                    // 버튼 바꿈
                    binding.startButton.visibility = View.GONE
                    binding.stopButton.visibility = View.VISIBLE

                    val second = intent?.getIntExtra(TrackRecordService.SECOND, 0)
                    binding.tvTime.text = Utils.timeToText(second)

                    val distance = intent?.getDoubleExtra(TrackRecordService.DISTANCE, 0.0)
                    binding.tvDistance.text = Utils.distanceToText(distance)

                    val avgSpeed = intent?.getDoubleExtra(TrackRecordService.AVG_SPEED, 0.0)
                    binding.tvAvgSpeed.text = Utils.avgSpeedToText(avgSpeed)


                    CoroutineScope(Dispatchers.Main).launch {
                        val gpsDataList = withContext(Dispatchers.IO) {
                            gpsDataDao.getAllGpsData() // withContext 의 반환값
                        }

                        println("db 에서 불러온 크기: ${gpsDataList.size}")

                        // 선 그리기
                        val latLngList = withContext(Dispatchers.Default) {
                            val latLngList = ArrayList<LatLng>()
                            for (gpsData in gpsDataList) {
                                latLngList.add(LatLng(gpsData.lat, gpsData.lng))
                                println("withContext 내부 for 문 수행 중")
                            }
                            latLngList
                        }

                        println("withContext 끝나고 내려옴")

                        mGoogleMap.addPolyline(PolylineOptions().addAll(latLngList)) // 그림 그림
                    }
                }

                TrackRecordService.RECORD_START_LAT_LNG -> { // 기록 시작 위치
                    println("업데이트 시작 위치 받음")
                    val recordStartLatLng = intent?.getParcelableExtra<LatLng>(TrackRecordService.LAT_LNG)!!
                    beforeLatLng = recordStartLatLng
                }

                TrackRecordService.AFTER_START_UPDATE -> { // 기록 시작 후 초마다 받는 업데이트
                    val second = intent?.getIntExtra(TrackRecordService.SECOND, 0)
                    binding.tvTime.text = Utils.timeToText(second)

                    val locationChanged = intent?.getBooleanExtra(TrackRecordService.LOCATION_CHANGED, true)

                    // 위치 다르면 관련 정보 수정하고 마커 이동하고 선 그림
                    if (locationChanged) {
                        val latLng = intent?.getParcelableExtra<LatLng>(TrackRecordService.LAT_LNG)!!

                        val distance = intent?.getDoubleExtra(TrackRecordService.DISTANCE, 0.0)
                        binding.tvDistance.text = Utils.distanceToText(distance)

                        val avgSpeed = intent?.getDoubleExtra(TrackRecordService.AVG_SPEED, 0.0)
                        binding.tvAvgSpeed.text = Utils.avgSpeedToText(avgSpeed)

                        mLocationMarker?.position = latLng // 마커 이동
                        mGoogleMap.addPolyline(PolylineOptions().add(beforeLatLng, latLng)) // 그림 그림

                        beforeLatLng = latLng

                        // 도착점 도착했는지 체크
                        val currentLocation = Location("currentLocation")
                        currentLocation.latitude = latLng.latitude
                        currentLocation.longitude = latLng.longitude
                        if (endPoint.distanceTo(currentLocation) < 20.0) {
                            // 서비스 종료하라고 커맨드 보냄
                            val intent = Intent(this@TrackRecordActivity, TrackRecordService::class.java)
                            intent.action = TrackRecordService.COMPLETE_RECORD
                            startForegroundService(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }
}