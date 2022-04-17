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
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.capstonandroid.*
import com.example.capstonandroid.databinding.ActivityTrackRecordBinding
import com.example.capstonandroid.db.AppDatabase
import com.example.capstonandroid.db.dao.GpsDataDao
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.Track
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.checkpoint_dialog.*
import kotlinx.coroutines.*
import retrofit2.Retrofit
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList


class TrackRecordActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.SnapshotReadyCallback {
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

    private lateinit var mPolyline: Polyline // 내가 달린 구간 폴리라인

    private lateinit var textToSpeech: TextToSpeech // tts
    private var textToSpeechInitialized = false // tts 초기화 됐는지

    // 트랙 관련
    private lateinit var trackPolyline: Polyline
    private var trackStartPointMarker: Marker? = null // 트랙 시작점 마커
    private var trackEndPointMarker: Marker? = null // 트랙 끝점 마커
    private lateinit var checkpointMarkerList: ArrayList<Marker>
    private lateinit var checkpointDialog: Dialog

    private lateinit var latLngList: ArrayList<LatLng> // 폴리라인에 넣을 위치 리스트
    private var mLocationMarker: Marker? = null // 내 위치 마커
    private var mLocationBack: Marker? = null

    private var inCanStartArea = false // 시작 가능한 범위 내에 있는지

    private var gotFirstLocation = false // 첫 번째 위치를 받아와 시작 가능한 상태인지

    private var second = 0

    @SuppressLint("NewApi", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityTrackRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        latLngList = ArrayList()

        // 체크포인트 커스텀 다이얼로그 초기화
        checkpointDialog = Dialog(this)
        checkpointDialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 제거
        checkpointDialog.setContentView(R.layout.checkpoint_dialog)

        checkpointMarkerList = ArrayList()

        // 인텐트로 넘어온 옵션값 받음
        val intent = intent
        exerciseKind = intent.getStringExtra("exerciseKind")!!
        trackId = intent.getStringExtra("trackId")!!
        println("exerciseKind $exerciseKind")
        println("trackId $trackId")

        textToSpeech = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.JAPANESE

                textToSpeechInitialized = true
            }
        }


        // db 사용 설정
        val db = AppDatabase.getInstance(applicationContext)!!
        gpsDataDao = db.gpsDataDao()

        job = Job() // job 생성

        initRetrofit()

        // 액티비티 이동 후 답을 받는 콜백
        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // 정상적으로 카운트다운 다 지나오면 시작
            if (result.resultCode == CountDownActivity.COUNT_DOWN_ACTIVITY_RESULT_CODE) {

                // 커맨드 보냄 (서비스는 한번 더 실행 안되니 커맨드가 보내진다.)
                val intent = Intent(this@TrackRecordActivity, TrackRecordService::class.java)
                intent.action = TrackRecordService.START_RECORD
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
        val intent = Intent(this@TrackRecordActivity, TrackRecordService::class.java)
        intent.action = TrackRecordService.STOP_SERVICE
        startForegroundService(intent)
        super.onBackPressed()
    }

    override fun onBackPressed() {
        println("onBackPressed 호출")
        // 달리기중 아닐때만 뒤로 갈 수 있게 함
        if (!TrackRecordService.isStarted) {
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
            // 맵부터 다 초기화되면 다음 프로세스로 넘어감.
            launch {
                initTrack()
            }.join()

            binding.tvInformation.text = "위치 정보 불러오는 중"
            binding.tvInformation.setBackgroundColor(resources.getColor(R.color.green))

            if (TrackRecordService.isStarted) {
                println("trackRecord 이미 실행중이다.")

                // 버튼 바꿈
                binding.startButton.visibility = View.GONE
                binding.stopButton.visibility = View.VISIBLE

                binding.tvInformation.visibility = View.GONE // 정보 창 없앰
                canStartAreaCircle.remove() // 시작 영역 제거

                // 기존 트랙은 투명하게
                trackPolyline.color = resources.getColor(R.color.no_selected_polyline_color, null)
                trackEndPointMarker?.alpha = 0.4F
                trackStartPointMarker?.alpha = 0.4F

                // 마지막 위치 가져오고 마커 생성
                beforeLatLng = LatLng(TrackRecordService.mLocation!!.latitude, TrackRecordService.mLocation!!.longitude)
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
                val intent = Intent(this@TrackRecordActivity, TrackRecordService::class.java)
                intent.action = TrackRecordService.START_PROCESS
                intent.putExtra("exerciseKind", exerciseKind)
                intent.putExtra("trackName", track.trackName)
                intent.putExtra("trackId", track._id)
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
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, IntentFilter(TrackRecordService.ACTION_BROADCAST))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        mGoogleMap.setMaxZoomPreference(18F) // 최대 줌

        checkPermission()
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
                println("${coordinate[1]}, ${coordinate[0]}")
            }
            trackPolyline = mGoogleMap.addPolyline(PolylineOptions()
                .clickable(true)
                .addAll(trackLatLngList)
                .color(ContextCompat.getColor(this, R.color.main_color))
                .width(Utils.POLYLINE_WIDTH))

            // 체크포인트 추가
            println("체크포인트")
            println(track.checkPoint)

            for (checkpointIndex in track.checkPoint) {

                val checkpointMarker = mGoogleMap.addMarker(MarkerOptions()
                    .position(LatLng(track.gps.coordinates[checkpointIndex][1], track.gps.coordinates[checkpointIndex][0]))
                    .title("체크포인트")
                    .icon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.checkpoint_before,null)))
                    .anchor(0.5F, 0.5F))!!
                checkpointMarkerList.add(checkpointMarker)
            }

            // 출발점 마커 추가
            trackStartPointMarker = mGoogleMap.addMarker(MarkerOptions()
                .position(LatLng(startPoint.latitude, startPoint.longitude))
                .title("출발점")
                .icon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.start_point_marker,null)))
                .anchor(0.5F, 0.9F))

            // 시작 가능 반경 그림
            canStartAreaCircle = mGoogleMap.addCircle(CircleOptions()
                .center(LatLng(startPoint.latitude, startPoint.longitude))
                .radius(20.0)
                .fillColor(resources.getColor(R.color.default_marker_color_opacity, null))
                .strokeWidth(0F))

            // 도착점 마커 추가
            trackEndPointMarker = mGoogleMap.addMarker(MarkerOptions()
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

    @SuppressLint("NewApi")
    private fun predictLocation() {
        CoroutineScope(Dispatchers.Main).launch {
            launch(Dispatchers.Default) {
                // 내 위치 예측
                var myIncreaseSumDistance = 0F // 한계치까지만 비교하기 위한 내 위치 증가깂

                var myBeforeLocation = Location("start") // 트랙 위에서 현재 내 위치라고 예상되는 지점
                myBeforeLocation.latitude =
                    track.gps.coordinates[TrackRecordService.myLocationIndexOnTrack][1]
                myBeforeLocation.longitude =
                    track.gps.coordinates[TrackRecordService.myLocationIndexOnTrack][0]

                var myMinDistance =
                    TrackRecordService.mLocation!!.distanceTo(myBeforeLocation) // 가장 작은 거리 차이인 지점과의 거리차이

                var myPredictedLocation = TrackRecordService.myLocationIndexOnTrack // 예상하는 트랙위의 내 위치

                // 이동할 수 있는 가장 최대 거리라고 생각하는 지점까지 반복하며 트랙 위에서 어디와 가장 가까운지 구함
                println("$myPredictedLocation ${track.gps.coordinates.size}")
                for (i in myPredictedLocation + 1 until track.gps.coordinates.size) {
                    var location = Location("flag")
                    location.latitude = track.gps.coordinates[i][1]
                    location.longitude = track.gps.coordinates[i][0]

                    // 이동할 수 있는 가장 최대 거리를 넘어서면 반복문 종료
                    myIncreaseSumDistance += myBeforeLocation.distanceTo(location)
                    if (myIncreaseSumDistance / (second - TrackRecordService.myBeforeLocationChangedSecond) >= TrackRecordService.MAX_DISTANCE) {
                        break
                    }

                    val distance = location.distanceTo(TrackRecordService.mLocation)
                    if (distance <= myMinDistance) {
                        myMinDistance = distance
                        myPredictedLocation = i
                    }

                    myBeforeLocation = location
                }

                // 내 트랙 위에서의 누적 이동 거리 갱신해줌
                for (i in TrackRecordService.myLocationIndexOnTrack until myPredictedLocation) {
                    val beforeLocation = Location("before")
                    beforeLocation.latitude = track.gps.coordinates[i][1]
                    beforeLocation.longitude = track.gps.coordinates[i][0]
                    val afterLocation = Location("after")
                    afterLocation.latitude = track.gps.coordinates[i + 1][1]
                    afterLocation.longitude = track.gps.coordinates[i + 1][0]
                    TrackRecordService.mySumDistanceOnTrack += beforeLocation.distanceTo(afterLocation)
                }

                TrackRecordService.myLocationIndexOnTrack = myPredictedLocation // 내 예상 지점 갱신
            }.join()

//            // 예상 지점
//            mGoogleMap.addMarker(MarkerOptions().position(LatLng(track.gps.coordinates[TrackRecordService.myLocationIndexOnTrack][1], track.gps.coordinates[TrackRecordService.myLocationIndexOnTrack][0])))

            // 끝에 도착했는지 여기서 체크하자
            println("끝에 도착했는지 체크: ${TrackRecordService.myLocationIndexOnTrack}, ${track.gps.coordinates.size-1}")
            if (TrackRecordService.myLocationIndexOnTrack == track.gps.coordinates.size-1) {
                // 종료하기전 스냅샷 찍음
                val builder: LatLngBounds.Builder = LatLngBounds.Builder() // 카메라 이동을 위한 빌더
                for (latLng in latLngList) {
                    builder.include(latLng) // 카메라안에 들어와야 하는 지점들 추가
                }
                // 카메라 업데이트
                val bounds: LatLngBounds = builder.build()
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))

                mGoogleMap.snapshot(this@TrackRecordActivity)
            }

            TrackRecordService.myBeforeLocationChangedSecond = second

            // 체크포인트 있으면 검사
            if (track != null)
            if (track.checkPoint.size > TrackRecordService.checkpointIndex) {
                if (TrackRecordService.myLocationIndexOnTrack >= track.checkPoint[TrackRecordService.checkpointIndex]) {
                    val token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!
                    val checkpointResponse = supplementService.checkpoint(token, TrackRecordService.checkpointIndex, trackId, second)
                    checkpointMarkerList[TrackRecordService.checkpointIndex].setIcon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.checkpoint_after,null)))
                    // 다이얼로그 띄움
                    checkpointDialog.checkpoint_pace.text = "上位${checkpointResponse.body()!!.rank.toInt()}パーセントのペースです。"
                    checkpointDialog.show()
                    checkpointDialog.window?.setBackgroundDrawable(
                        ColorDrawable(Color.TRANSPARENT)
                    )
                    Handler(mainLooper).postDelayed({
                        checkpointDialog.dismiss()
                    }, 3000)
                    if (textToSpeechInitialized) {
                        textToSpeech.speak("上位${checkpointResponse.body()!!.rank.toInt()}パーセントです。", TextToSpeech.QUEUE_FLUSH, null, "abc")
                    }
                    TrackRecordService.checkpointIndex += 1
                }
            }
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
        @SuppressLint("NewApi")
        override fun onReceive(context: Context?, intent: Intent?) {

            // flag 에 따라 분기처리
            when (intent?.getStringExtra("flag")) {
                TrackRecordService.BEFORE_START_LOCATION_UPDATE -> { // 시작 전 위치 업데이트
                    val latLng = intent?.getParcelableExtra<LatLng>(TrackRecordService.LAT_LNG)!!
                    println("리시버로 위치 받음 ${latLng.latitude}, ${latLng.longitude}")

                    if (!gotFirstLocation) { // 처음 받은 위치면 초기 설정
                        gotFirstLocation = true

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

                        binding.tvInformation.text = "시작 가능 위치로 이동하세요."
                        binding.tvInformation.setBackgroundColor(resources.getColor(R.color.red))
                    } else { // 처음받은 위치 아니면 마커만 옮김
                        mLocationMarker?.position = latLng // 마커 이동
                        mLocationBack?.position = latLng
                    }

                    // 시작 가능 위치인지 확인
                    val mLocation = Location("myLocation")
                    mLocation.latitude = latLng.latitude
                    mLocation.longitude = latLng.longitude

                    inCanStartArea = mLocation.distanceTo(startPoint) < 20.0
//                    inCanStartArea = true
                    println("시작 가능 위치 내인지: $inCanStartArea")
                    if (inCanStartArea) {
                        binding.tvInformation.visibility = View.GONE
                    } else {
                        binding.tvInformation.visibility = View.VISIBLE
                    }
                }
                TrackRecordService.RECORD_START_LAT_LNG -> { // 기록 시작 위치
                    println("업데이트 시작 위치 받음")
                    val recordStartLatLng = intent?.getParcelableExtra<LatLng>(TrackRecordService.LAT_LNG)!!
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

                TrackRecordService.AFTER_START_UPDATE -> { // 기록 시작 후 초마다 받는 업데이트
                    second = intent?.getIntExtra(TrackRecordService.SECOND, 0)
                    binding.tvTime.text = Utils.timeToText(second)

                    val avgSpeed = intent?.getDoubleExtra(TrackRecordService.AVG_SPEED, 0.0)
                    binding.tvAvgSpeed.text = Utils.formatDoublePointTwo(avgSpeed)

                    val calorie = intent?.getDoubleExtra(TrackRecordService.CALORIE, 0.0)
                    binding.tvKcal.text = Utils.formatDoublePointTwo(calorie)

                    val locationChanged = intent?.getBooleanExtra(TrackRecordService.LOCATION_CHANGED, true)

                    if (locationChanged) {
                        val latLng = intent?.getParcelableExtra<LatLng>(TrackRecordService.LAT_LNG)!!

                        val distance = intent?.getDoubleExtra(TrackRecordService.DISTANCE, 0.0)
                        binding.tvDistance.text = Utils.distanceToText(distance)

                        mLocationMarker?.position = latLng // 마커 이동
                        mLocationBack?.position = latLng

                        latLngList.add(latLng)
                        mPolyline.points = latLngList

                        beforeLatLng = latLng

                        predictLocation()
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

        val intent = Intent(this@TrackRecordActivity, TrackRecordService::class.java)
        intent.action = TrackRecordService.COMPLETE_RECORD
        startForegroundService(intent)
        finish()
    }
}