package com.example.capstonandroid.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.*
import com.example.capstonandroid.R
import com.example.capstonandroid.adapter.FriendlyMatchingRecyclerViewAdapter
import com.example.capstonandroid.databinding.ActivitySelectTrackBinding
import com.example.capstonandroid.network.dto.Track
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.dto.Post
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.*
import retrofit2.Retrofit

class SelectTrackActivity : AppCompatActivity(), OnMapReadyCallback, SelectExerciseKindBottomSheetClickListener {

    private var _binding: ActivitySelectTrackBinding? = null
    private val binding get() = _binding!!

    private lateinit var mGoogleMap: GoogleMap

    private lateinit var retrofit: Retrofit // 레트로핏 인스턴스
    private lateinit var supplementService: BackendApi // api

    private lateinit var persistentBottomSheet: BottomSheetBehavior<View>

    private lateinit var rankMatchingDialog: Dialog // 랭크매치 다이얼로그
    private lateinit var friendlyMatchingDialog: Dialog // 친선전 다이얼로그

    private lateinit var mFusedLocationClient: FusedLocationProviderClient // 통합 위치 제공자 핸들

    private lateinit var mLocationCallback: LocationCallback // 위치 정보 업데이트 콜백

    private lateinit var mLocation: Location // 내 위치

    private lateinit var job: Job // 코루틴 동작을 제어하기 위한 job

    // 친선전 리사이클러뷰 구현을 위해
    private var page = 1 // 현재 페이지
    private var isNext = false // 다음 페이지 있는지
    private var isLoading = false
    private lateinit var friendlyMatchingItemList: ArrayList<Post?>
    private lateinit var friendlyMatchingRecyclerViewAdapter: FriendlyMatchingRecyclerViewAdapter
    private lateinit var friendlyMatchingRecyclerView: RecyclerView

    private lateinit var trackMap: HashMap<String, Track> // 트랙 맵
    private lateinit var markerMap: HashMap<String, Marker> // 마커 맵
    private lateinit var polylineMap: HashMap<String, Polyline> // 폴리라인 관리를 위한 맵

    private var mLocationMarker: Marker? = null // 내 위치 마커
    private var mLocationBack: Marker? = null

    private var selectedTrackId: String? = null // 선택된 트랙 인덱스

    private var exerciseKind = "R" // 운동 종류

    private lateinit var trackMarker: View // 커스텀 마커 뷰
    private lateinit var trackMarkerTextView: TextView // 커스텀 마커 텍스트 뷰

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivitySelectTrackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "トラック選択" // 액션바 텍스트 수정

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this) // 통합 위치 제공자 초기화

        // 위치 요청 응답 왔을 때 콜백
        mLocationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                mLocation = locationResult.lastLocation
                mLocationMarker?.position = LatLng(mLocation.latitude, mLocation.longitude) // 마커 이동
                mLocationBack?.position = LatLng(mLocation.latitude, mLocation.longitude)
            }
        }

        // 랭크매치 커스텀 다이얼로그 초기화
        rankMatchingDialog = Dialog(this)
        rankMatchingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 제거
        rankMatchingDialog.setContentView(R.layout.rank_matching_dialog)

        job = Job() // job 생성

        initRetrofit() // retrofit 인스턴스 초기화

        trackMap = HashMap() // 트랙 담아놓을 맵 초기화
        markerMap = HashMap() // 마커 담아놓을 맵 초기화
        polylineMap = HashMap() // 폴리라인 담아놓을 맵 초기화

        trackMarker = LayoutInflater.from(this).inflate(R.layout.track_and_name, null)!!
        trackMarkerTextView = trackMarker.findViewById(R.id.tv_marker) as TextView

        // 내 mmr 기입
        CoroutineScope(Dispatchers.Main).launch {
            val token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!
            val userResponse = supplementService.getUser(token)
            if (userResponse.isSuccessful) {
                binding.tvMmr.text = userResponse.body()!!.mmr.toString()
            }
        }

        // 운동 종목 버튼 초기화
        binding.btnSelectExerciseKind.setOnClickListener {
            val selectedId = when (exerciseKind) {
                "R" -> {
                    R.id.radio_button_running
                }
                "B" -> {
                    R.id.radio_button_cycling
                }
                else -> {
                    0
                }
            }
            SelectExerciseKindBottomSheetDialog.newInstance(selectedId).show(supportFragmentManager, "SelectExerciseKindBottomSheetDialog")
        }

        // 트랙 상세정보 보기 버튼 초기화
        binding.btnTrackInformation.setOnClickListener {
            // 매치 타입에 따라 분기처리
            val intent = Intent(this, TrackActivity::class.java)
            intent.putExtra("trackId", selectedTrackId)
            startActivity(intent)
        }

        // 현재 위치에서 검색 버튼 초기화
        binding.buttonSearchTrack.setOnClickListener {
            initTracks()
        }

        // 혼자하기 눌렀을때 리스너 등록
        binding.buttonNormal.setOnClickListener {
            val intent = Intent(this, TrackRecordActivity::class.java)
            intent.putExtra("trackId", selectedTrackId)
            intent.putExtra("exerciseKind", exerciseKind)
            startActivity(intent)
            finish()
        }

        // 친선전 눌렀을때 리스너 등록
        binding.buttonFriendly.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                // 초기화
                page = 1 // 현재 페이지
                isNext = false // 다음 페이지 있는지
                isLoading = false
                friendlyMatchingItemList = ArrayList()

                // 친선전 커스텀 다이얼로그 초기화
                friendlyMatchingDialog = Dialog(this@SelectTrackActivity)
                friendlyMatchingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                friendlyMatchingDialog.setContentView(R.layout.friendly_matching_dialog)

                friendlyMatchingRecyclerView = friendlyMatchingDialog.findViewById(R.id.recycler_view_friendly_match)

                friendlyMatchingRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (isNext) {
                            if (!isLoading) {
                                if ((recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition() == friendlyMatchingItemList.size - 1) {
                                    getMoreFriendlyMatching()
                                    isLoading = true
                                }
                            }
                        }
                    }
                })

                // 다이얼로그 띄움
                friendlyMatchingDialog.show()
                friendlyMatchingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                // 초기값 받아옴
                val token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!
                val friendlyMatchingResponse = supplementService.friendlyMatching(token, selectedTrackId!!, page)
                if (friendlyMatchingResponse.isSuccessful) {
                    if (friendlyMatchingResponse.body()!!.followPostList.total == 0) {
                        isNext = false
                        Toast.makeText(this@SelectTrackActivity, "フォロワー, 自分の中でこのトラックを走った記録がありません", Toast.LENGTH_SHORT).show()
                    } else {
                        val friendlyMatchingList = friendlyMatchingResponse.body()!!.followPostList.data
                        for (friendlyMatch in friendlyMatchingList) {
                            friendlyMatchingItemList.add(friendlyMatch)
                        }
                        if (friendlyMatchingResponse.body()!!.followPostList.next_page_url != null) {
                            page += 1
                            isNext = true
                        } else {
                            isNext = false
                        }
                    }
                }
                friendlyMatchingRecyclerViewAdapter = FriendlyMatchingRecyclerViewAdapter(friendlyMatchingItemList)
                friendlyMatchingRecyclerView.adapter = friendlyMatchingRecyclerViewAdapter
                // 아이템 클릭 리스너 등록
                friendlyMatchingRecyclerViewAdapter.setOnItemClickListener(object : FriendlyMatchingRecyclerViewAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        AlertDialog.Builder(this@SelectTrackActivity)
                            .setTitle("親善競技")
                            .setMessage("${friendlyMatchingItemList[position]!!.user.name}と一緒に走りますか")
                            .setPositiveButton("はい") { _, _ ->
                                val intent = Intent(this@SelectTrackActivity, TrackPaceMakeActivity::class.java)
                                intent.putExtra("matchType", "친선")
                                intent.putExtra("exerciseKind", exerciseKind)
                                intent.putExtra("trackId", selectedTrackId)
                                intent.putExtra("opponentGpsDataId", friendlyMatchingItemList[position]!!.gps_id)
                                intent.putExtra("opponentPostId", friendlyMatchingItemList[position]!!.id)
                                intent.putExtra("opponentAvgSpeed", friendlyMatchingItemList[position]!!.average_speed)
                                intent.putExtra("opponentTime", friendlyMatchingItemList[position]!!.time)
                                intent.putExtra("opponentProfileImage", friendlyMatchingItemList[position]!!.user.profile)
                                startActivity(intent)

                                friendlyMatchingDialog.dismiss()
                                finish()
                            }
                            .setNegativeButton("キャンセル") {_, _ ->

                            }
                            .create()
                            .show()
                    }
                })
            }
        }
        // 랭크전 눌렀을때 리스너 등록
        binding.buttonRank.setOnClickListener {

            CoroutineScope(Dispatchers.Main).launch {
                val token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!
                val rankMatchingResponse = supplementService.rankMatching(token, selectedTrackId!!)
                if (rankMatchingResponse.isSuccessful) {
                    when (rankMatchingResponse.code()) {
                        200 -> {
                            val post = rankMatchingResponse.body()!!.post
                            val user = rankMatchingResponse.body()!!.user

                            val userNameTextView: TextView = rankMatchingDialog.findViewById(R.id.tv_vs_user_name)
                            userNameTextView.text = user.name
                            val mmrTextView: TextView = rankMatchingDialog.findViewById(R.id.tv_vs_mmr)
                            mmrTextView.text = post.mmr.toString()
                            val postNameTextView: TextView = rankMatchingDialog.findViewById(R.id.tv_vs_post_name)
                            postNameTextView.text = post.title
                            val avgSpeedTextView: TextView = rankMatchingDialog.findViewById(R.id.tv_vs_avg_speed)
                            avgSpeedTextView.text = "${Utils.formatDoublePointTwo(post.average_speed)}km"
                            val timeTextView: TextView = rankMatchingDialog.findViewById(R.id.tv_vs_time)
                            timeTextView.text = Utils.timeToText(post.time)

                            rankMatchingDialog.show()
                            rankMatchingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 모서리 둥글게 하기 위해서

                            delay(3000)

                            val intent = Intent(this@SelectTrackActivity, TrackPaceMakeActivity::class.java)
                            intent.putExtra("matchType", "랭크")
                            intent.putExtra("exerciseKind", exerciseKind)
                            intent.putExtra("trackId", selectedTrackId)
                            intent.putExtra("opponentGpsDataId", post.gps_id)
                            intent.putExtra("opponentPostId", post.id)
                            intent.putExtra("opponentAvgSpeed", post.average_speed)
                            intent.putExtra("opponentTime", post.time)
                            intent.putExtra("opponentProfileImage", user.profile)
                            startActivity(intent)

                            rankMatchingDialog.dismiss()
                            finish()
                        }
                        204 -> {
                            Toast.makeText(this@SelectTrackActivity, "적당한 매칭 상대가 없습니다.", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }
        }

        // 밑에서 올라오는 바텀시트 설정
        persistentBottomSheet = BottomSheetBehavior.from(binding.bottomSheet)
        persistentBottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> { // 반만 펼쳤을 때
                        println("bottom sheet: STATE_COLLAPSED")
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        println("bottom sheet: STATE_DRAGGING")
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> { // 완전히 펼쳤을 때
                        println("bottom sheet: STATE_EXPANDED")
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        println("bottom sheet: STATE_HALF_EXPANDED")
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> { // bottom sheet 가 사라졌을 때
                        println("bottom sheet: STATE_HIDDEN")
                        selectedTrackId = null
                        // 투명도 다 되돌려 줌
                        for (trackId in trackMap.keys) {
                            polylineMap[trackId]?.color = ContextCompat.getColor(this@SelectTrackActivity, R.color.main_color)
                            markerMap[trackId]?.alpha = 1F
                        }
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        println("bottom sheet: STATE_SETTLING")
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                println("bottom sheet: onSlide")
            }
        })

        persistentBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN // 초기 상태는 bottom sheet 내려가 있는 상태로

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getMoreFriendlyMatching() {
        val runnable = Runnable {
            friendlyMatchingItemList.add(null)
            friendlyMatchingRecyclerViewAdapter.notifyItemInserted(friendlyMatchingItemList.size - 1)
        }
        friendlyMatchingRecyclerView.post(runnable)

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)

            friendlyMatchingItemList.removeAt(friendlyMatchingItemList.size - 1)
            friendlyMatchingRecyclerViewAdapter.notifyItemRemoved(friendlyMatchingItemList.size)

            val token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!
            val friendlyMatchingResponse = supplementService.friendlyMatching(token, selectedTrackId!!, page)

            if (friendlyMatchingResponse.isSuccessful) {
                val friendlyMatchingList = friendlyMatchingResponse.body()!!.followPostList.data
                for (friendlyMatching in friendlyMatchingList) {
                    friendlyMatchingItemList.add(friendlyMatching)
                }
                friendlyMatchingRecyclerViewAdapter.notifyItemRangeInserted((page - 1) * friendlyMatchingResponse.body()!!.followPostList.per_page, friendlyMatchingResponse.body()!!.followPostList.to)
                isLoading = false
                if (friendlyMatchingResponse.body()!!.followPostList.next_page_url != null) {
                    page += 1
                    isNext = true
                } else {
                    isNext = false
                }
            }
        }
    }

    // 보이는 지도에 맞게 트랙 가져와 지도에 그려 줌
    private fun initTracks() {
        CoroutineScope(Dispatchers.Main + job).launch {
            println("실행됨")

            // 초기화
            selectedTrackId = null
            trackMap.clear()
            for ((_, marker) in markerMap) {
                marker.remove()
            }
            markerMap.clear()
            for ((_, polyline) in polylineMap) {
                polyline.remove()
            }
            polylineMap.clear()

            // 보고있는 카메라 위치 측정
            val latLngBounds = mGoogleMap.projection.visibleRegion.latLngBounds
            println("위치 경계: ${latLngBounds.southwest.longitude} ${latLngBounds.southwest.latitude} ${latLngBounds.northeast.longitude} ${latLngBounds.northeast.latitude}")

            // 현재 지도상에 보이는 트랙 가져오는 api 호출
            val token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!
            val tracksResponse = supplementService.getTracks(token, latLngBounds.southwest.longitude, latLngBounds.southwest.latitude, latLngBounds.northeast.longitude, latLngBounds.northeast.latitude, 16, exerciseKind)
            println("응답 옴 ${tracksResponse.body()}")
            if (tracksResponse.isSuccessful) {

                // 1개 이상 응답이 오면 맵에 데이터 보관하고 구글맵에 그린다.
                for (track in tracksResponse.body()!!.result) {

                    trackMap[track._id] = track

                    // 마커 추가
                    trackMarkerTextView.text = track.trackName
                    val marker = mGoogleMap.addMarker(MarkerOptions()
                        .position(LatLng(track.gps.coordinates[0][1], track.gps.coordinates[0][0]))
                        .title(track.trackName)
                        .icon(BitmapDescriptorFactory.fromBitmap(Utils.createBitmapFromView(trackMarker)))
                        .anchor(0.5F, 0.9F))
                    marker!!.tag = track._id
                    markerMap[track._id] = marker!!

                    // 폴리라인 추가
                    val latLngList = ArrayList<LatLng>()
                    launch(Dispatchers.Default) {
                        for (coordinate in track.gps.coordinates) {
                            latLngList.add(LatLng(coordinate[1], coordinate[0]))
                            println("${coordinate[1]}, ${coordinate[0]}")
                        }
                    }.join() // 오래걸릴수 있으니까 백그라운드 스레드로 넘겨봄...

                    val polyline = mGoogleMap.addPolyline(PolylineOptions()
                        .clickable(true)
                        .addAll(latLngList)
                        .width(Utils.POLYLINE_WIDTH)
                        .color(ContextCompat.getColor(this@SelectTrackActivity, R.color.main_color)))
                    polyline.tag = track._id
                    polylineMap[track._id] = polyline
                }
                // 받아온 트랙이 있나 없나에 따라 분기처리
                if (trackMap.count() > 0) {
                    // 가장 가까운 곳 찾아서 초기값으로 세팅해 줌
                    val startSelectedTrackId = withContext(Dispatchers.Default) {
                        var minDistance = Float.MAX_VALUE
                        var resultTrackId = ""
                        for ((trackId, track) in trackMap) {
                            val startLocation = Location("startPoint")
                            startLocation.latitude = track.gps.coordinates[0][1]
                            startLocation.longitude = track.gps.coordinates[0][0]
                            val distance = mLocation.distanceTo(startLocation)
                            if (minDistance > distance) {
                                minDistance = distance
                                resultTrackId = trackId
                            }
                        }
                        resultTrackId
                    }
                    selectTrack(startSelectedTrackId)
                } else {
                    persistentBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
                    Toast.makeText(this@SelectTrackActivity, "이 구역에는 트랙이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
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
        mGoogleMap.setMaxZoomPreference(18F) // 최대 줌
        mGoogleMap.uiSettings.isCompassEnabled = true // 나침반 설정

        // 마커 클릭 리스너 등록
        mGoogleMap.setOnMarkerClickListener { marker ->
            if (marker.tag != "myLocation" && marker.tag != "myLocationBack") {
                selectTrack(marker.tag.toString())
            }
            true
        }

        // 폴리라인 클릭 리스너 등록
        mGoogleMap.setOnPolylineClickListener { polyline ->
            selectTrack(polyline.tag.toString())
        }

        checkPermission()
    }

    // 마커 클릭했을 때 처리
    private fun selectTrack(newSelectedTrackId: String?) {
        selectedTrackId = newSelectedTrackId
        println(selectedTrackId)
        binding.tvTrackTitle.text = trackMap[selectedTrackId]?.trackName
        binding.tvTrackDistance.text = "${"%.2f".format(trackMap[selectedTrackId]!!.totalDistance)}km"
        binding.tvTrackSlope.text = "${trackMap[selectedTrackId]?.avgSlope}%"
        persistentBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED

        // 투명도 조절로 선택된 느낌 줌
        for (trackId in trackMap.keys) {
            if (trackId != selectedTrackId) {
                markerMap[trackId]?.alpha = 0.4F
                polylineMap[trackId]?.color = ContextCompat.getColor(this, R.color.no_selected_polyline_color)
            } else {
                markerMap[trackId]?.alpha = 1F
                polylineMap[trackId]?.color = ContextCompat.getColor(this, R.color.main_color)
            }
        }

        // 카메라 업데이트
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(trackMap[selectedTrackId]!!.gps.coordinates[0][1], trackMap[selectedTrackId]!!.gps.coordinates[0][0]), mGoogleMap.cameraPosition.zoom))
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

                mLocationMarker = mGoogleMap.addMarker(MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .icon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.circle_basic_marker, null)))
                    .anchor(0.5F, 0.5F))
                mLocationMarker?.tag = "myLocation"

                mLocationBack = mGoogleMap.addMarker(MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .icon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.circle_basic_marker_back, null)))
                    .alpha(0.3F)
                    .anchor(0.5F, 0.5F))
                mLocationBack?.tag = "myLocationBack"

                // 화면 이동하고 화면 이동 끝났을 때 맵 불러오는 콜백
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15.0f), object : GoogleMap.CancelableCallback {
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

    override fun onBackPressed() {
        if (persistentBottomSheet.state != BottomSheetBehavior.STATE_HIDDEN) {
            persistentBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            super.onBackPressed()
        }
    }

    override fun onRadioButtonChanged(selectedId: Int) {
        println("선택된 것: $selectedId")

        when (selectedId) {
            R.id.radio_button_running -> {
                exerciseKind = "R"
                binding.btnSelectExerciseKind.text = "ランニング"
                binding.btnSelectExerciseKind.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.run, null), null, null, null)
                binding.trackExerciseKindIcon.setImageResource(R.drawable.run)
                initTracks()
            }
            R.id.radio_button_cycling -> {
                exerciseKind = "B"
                binding.btnSelectExerciseKind.text = "サイクリング"
                binding.btnSelectExerciseKind.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.cycle, null), null, null, null)
                binding.trackExerciseKindIcon.setImageResource(R.drawable.cycle)
                initTracks()
            }
        }
    }
}
