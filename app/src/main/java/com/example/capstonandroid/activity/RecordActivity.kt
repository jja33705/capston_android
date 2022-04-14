package com.example.capstonandroid.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.capstonandroid.*
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityRecordBinding
import com.example.capstonandroid.db.AppDatabase
import com.example.capstonandroid.db.dao.GpsDataDao
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.*
import java.io.FileOutputStream

const val LOCATION_PERMISSION_REQUEST_CODE = 100 // 위치 권한 요청 코드

class RecordActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.SnapshotReadyCallback {

    private var _binding: ActivityRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var mGoogleMap: GoogleMap // 구글맵 선언

    private lateinit var gpsDataDao: GpsDataDao // db dao 핸들

    private lateinit var exerciseKind: String // 운동 종류

    private lateinit var mBroadcastReceiver: MBroadcastReceiver // 브로드캐스트 리시버

    private lateinit var beforeLatLng: LatLng // 선 긋기 시작 위치

    private lateinit var job: Job

    private lateinit var latLngList: ArrayList<LatLng> // 폴리라인에 넣을 위치 리스트
    private lateinit var mPolyline: Polyline
    private var mLocationMarker: Marker? = null // 내 위치 마커
    private var mLocationBack: Marker? = null // 내 위치 뒤

    private var gotFirstLocation = false // 서비스 다 초기화하고 위치정보 받아와서 시작 가능한 상태인지

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        latLngList = ArrayList()

        val intent: Intent = intent
        exerciseKind = intent.getStringExtra("exerciseKind")!!
        println("RecordActivity: 액티비티 시작할 때 운동 종류 받음 $exerciseKind")

        // db 사용 설정
        val db = AppDatabase.getInstance(applicationContext)!!
        gpsDataDao = db.gpsDataDao()

        job = Job()

        // 액티비티 이동 후 답을 받는 콜백
        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // 정상적으로 카운트다운 다 지나오면 시작
            if (result.resultCode == CountDownActivity.COUNT_DOWN_ACTIVITY_RESULT_CODE) {

                // 커맨드 보냄 (서비스는 한번 더 실행 안되니 커맨드가 보내진다.)
                val intent = Intent(this@RecordActivity, RecordService::class.java)
                intent.action = RecordService.START_RECORD
                startForegroundService(intent)

                // 버튼 바꿈
                binding.startButton.visibility = View.GONE
                binding.stopButton.visibility = View.VISIBLE
            }
        }

        // 시작 버튼 초기화
        binding.startButton.setOnClickListener{
            println("시작 버튼 클릭함")
            if (gotFirstLocation) {
                val intent = Intent(this, CountDownActivity::class.java)
                activityResultLauncher.launch(intent)

            } else {
                Toast.makeText(this@RecordActivity, "위치 정보 초기화 중", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // 종료 버튼 초기화
        binding.stopButton.setOnClickListener {
            println("종료 버튼 클릭함")

            // 종료하기전 스냅샷 찍음
            val builder: LatLngBounds.Builder = LatLngBounds.Builder() // 카메라 이동을 위한 빌더
            for (latLng in latLngList) {
                builder.include(latLng) // 카메라안에 들어와야 하는 지점들 추가
            }
            // 카메라 업데이트
            val bounds: LatLngBounds = builder.build()
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))

            mGoogleMap.snapshot(this)
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

        // 이미 서비스가 돌아가고 있다면 이전위치 먼저 등록
        if (RecordService.isStarted) {
            println("record 이미 실행 중이다")
            // 버튼 바꿈
            binding.startButton.visibility = View.GONE
            binding.stopButton.visibility = View.VISIBLE

            binding.tvInformation.visibility = View.GONE // 정보 창 없앰

            // 마지막 위치 가져오고 마커 생성
            beforeLatLng = LatLng(RecordService.mLocation.latitude, RecordService.mLocation.longitude)
            mLocationMarker = mGoogleMap.addMarker(MarkerOptions()
                .position(beforeLatLng)
                .icon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.circle_basic_marker, null)))
                .anchor(0.5F, 0.5F))

            mLocationBack = mGoogleMap.addMarker(MarkerOptions()
                .position(beforeLatLng)
                .icon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.circle_basic_marker_back, null)))
                .alpha(0.3F)
                .anchor(0.5F, 0.5F))

            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(beforeLatLng, 18.0f)) // 화면 이동

            loadGpsDataFromDatabaseAndDrawPolyline()
        } else {
            registerLocalBroadcastReceiver()

            // 서비스 시작
            val intent = Intent(this@RecordActivity, RecordService::class.java)
            intent.action = RecordService.START_PROCESS
            intent.putExtra("exerciseKind", exerciseKind)
            startForegroundService(intent)
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

            registerLocalBroadcastReceiver()
        }
    }

    // 브로드캐스트 리시버 등록
    private fun registerLocalBroadcastReceiver() {
        mBroadcastReceiver = MBroadcastReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, IntentFilter(RecordService.ACTION_BROADCAST))
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
        if (!RecordService.isStarted) {
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
        @SuppressLint("NewApi")
        override fun onReceive(context: Context?, intent: Intent?) {
            // flag 에 따라 분기처리
            when (intent?.getStringExtra("flag")) {
                RecordService.BEFORE_START_LOCATION_UPDATE -> { // 시작 전 위치 업데이트
                    val latLng = intent?.getParcelableExtra<LatLng>(RecordService.LAT_LNG)!!
                    println("리시버로 위치 받음 ${latLng.latitude}, ${latLng.longitude}")

                    mLocationMarker?.position = latLng // 마커 이동
                    mLocationBack?.position = latLng

                    // 이떄부터 기록 시작 가능 (마지막 위치는 부정확안 경향이 있다.)
                    if (!gotFirstLocation) {
                        gotFirstLocation = true

                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f)) // 화면 이동

                        // 내 위치 마커 생성
                        mLocationMarker = mGoogleMap.addMarker(MarkerOptions()
                            .position(latLng)
                            .icon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.circle_basic_marker, null)))
                            .anchor(0.5F, 0.5F))

                        mLocationBack = mGoogleMap.addMarker(MarkerOptions()
                            .position(latLng)
                            .icon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.circle_basic_marker_back, null)))
                            .alpha(0.3F)
                            .anchor(0.5F, 0.5F))

                        binding.tvInformation.visibility = View.GONE
                    }
                }
                RecordService.RECORD_START_LAT_LNG -> { // 기록 시작 위치
                    println("업데이트 시작 위치 받음")
                    val recordStartLatLng = intent?.getParcelableExtra<LatLng>(RecordService.LAT_LNG)!!
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
                RecordService.AFTER_START_UPDATE -> { // 기록 시작 후 초마다 받는 업데이트
                    val second = intent?.getIntExtra(RecordService.SECOND, 0)
                    binding.tvTime.text = Utils.timeToText(second)

                    val avgSpeed = intent?.getDoubleExtra(RecordService.AVG_SPEED, 0.0)
                    binding.tvAvgSpeed.text = Utils.formatDoublePointTwo(avgSpeed)

                    val calorie = intent?.getDoubleExtra(RecordService.CALORIE, 0.0)
                    binding.tvKcal.text = Utils.formatDoublePointTwo(calorie)

                    val locationChanged = intent?.getBooleanExtra(RecordService.LOCATION_CHANGED, true)

                    if (locationChanged) {
                        val latLng = intent?.getParcelableExtra<LatLng>(RecordService.LAT_LNG)!!

                        val distance = intent?.getDoubleExtra(RecordService.DISTANCE, 0.0)
                        binding.tvDistance.text = Utils.distanceToText(distance)

                        mLocationMarker?.position = latLng // 마커 이동
                        mLocationBack?.position = latLng

                        // 폴리라인 새로 그림
                        latLngList.add(latLng)
                        mPolyline.points = latLngList

                        beforeLatLng = latLng
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    override fun onSnapshotReady(snapshot: Bitmap?) {
        //앱 내부 cache 저장소: /data/user/0/com.example.capstonandroid/cache

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

        val intent = Intent(this@RecordActivity, RecordService::class.java)
        intent.action = RecordService.COMPLETE_RECORD
        startForegroundService(intent)

        finish()
    }
}