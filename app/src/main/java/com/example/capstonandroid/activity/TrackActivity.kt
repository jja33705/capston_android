package com.example.capstonandroid.activity

import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.capstonandroid.R
import com.example.capstonandroid.Utils
import com.example.capstonandroid.databinding.ActivityTrackBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.MyRankingResponse
import com.example.capstonandroid.network.dto.Post
import com.example.capstonandroid.network.dto.Track
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class TrackActivity : AppCompatActivity(), OnMapReadyCallback {
    private var _binding: ActivityTrackBinding? = null
    private val binding get() = _binding!!

    private lateinit var mGoogleMap: GoogleMap

    private lateinit var retrofit: Retrofit // 레트로핏 인스턴스
    private lateinit var supplementService: BackendApi // api

    private lateinit var trackMarker: View // 커스텀 마커 뷰
    private lateinit var trackMarkerTextView: TextView // 커스텀 마커 텍스트 뷰

    private lateinit var trackId: String

    private lateinit var track: Track

    private lateinit var firstRank: Post

    // 시작점, 끝점
    private lateinit var startLatLng: LatLng
    private lateinit var endLatLng: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityTrackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "トラック"

        val intent = intent
        trackId = intent.getStringExtra("trackId")!!

        initRetrofit()

        trackMarker = LayoutInflater.from(this).inflate(R.layout.track_and_name, null)!!
        trackMarkerTextView = trackMarker.findViewById(R.id.tv_marker) as TextView

        // 전체 랭킹 버튼 눌렀을 때
        binding.buttonAllRank.setOnClickListener {
            val intent = Intent(this@TrackActivity, RankingActivity::class.java)
            intent.putExtra("trackId", trackId)
            startActivity(intent)
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // 레트로핏 초기화
    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        CoroutineScope(Dispatchers.Main).launch {
            val token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!
            // 트랙 가져오기
            val trackResponse = supplementService.getTrack(token, trackId)
            if (trackResponse.isSuccessful) {
                track = trackResponse.body()!!

                binding.tvTrackTitle.text = track.trackName
                binding.tvTrackDistance.text = "${"%.2f".format(track.totalDistance)}km"
                binding.tvTrackDescription.text = track.description
                binding.tvTrackSlope.text = "${track.avgSlope}%"
                if (track.event == "B") {
                    binding.trackExerciseKindIcon.setImageResource(R.drawable.cycle)
                }

                startLatLng = LatLng(track.gps.coordinates[0][1], track.gps.coordinates[0][0])
                endLatLng = LatLng(track.gps.coordinates[track.gps.coordinates.size - 1][1], track.gps.coordinates[track.gps.coordinates.size - 1][0])

                drawTrack() // 트랙 그림

            } else {
                // 통신에러발생했을경우 처리해야함
            }

            // 랭킹 가져오기
            val rankingResponse = supplementService.getRanking(token, trackId, 1)
            if (rankingResponse.isSuccessful) {
                when (rankingResponse.code()) {
                    200 -> {
                        firstRank = rankingResponse.body()!!.data[0]

                        binding.trackRankFirstName.text = firstRank.user.name
                        binding.trackRankFirstTime.text = Utils.timeToText(firstRank.time)

                        val defaultImage = R.drawable.profile
                        val profileImageUrl = firstRank.user.profile

                        Glide.with(this@TrackActivity)
                            .load(profileImageUrl) // 불러올 이미지 url
                            .placeholder(defaultImage) // 이미지 로딩 시작하기 전 표시할 이미지
                            .error(defaultImage) // 로딩 에러 발생 시 표시할 이미지
                            .fallback(defaultImage) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                            .circleCrop()
                            .into(binding.trackRankFirstUserImage)
                    }
                    204 -> { // 아직 달린 사람 아무도 없을 때
                        binding.trackRankFirstLinearLayout.visibility = View.GONE
                        binding.tvTrackNoRecord.visibility = View.VISIBLE
                    }
                }
            }

            // 내 랭킹 가져오기
            val myRankingResponse = supplementService.getMyRanking(token, trackId)
            if (myRankingResponse.isSuccessful) {
                when (myRankingResponse.code()) {
                    200 -> {
                        val myRankingPost = myRankingResponse.body()!!.post
                        binding.tvMyAvgSpeed.text = "${Utils.formatDoublePointTwo(myRankingPost.average_speed)}km/h"
                        binding.tvMyDate.text = myRankingPost.date
                        binding.tvMyTime.text = Utils.timeToText(myRankingPost.time)
                        binding.tvMyRank.text = myRankingResponse.body()!!.rank.toString()
                        binding.tvMyTitle.text = myRankingPost.title

                        binding.linearLayoutMyRanking.visibility = View.VISIBLE
                    }
                    204 -> {
                    }
                }
            }
        }
    }

    private fun drawTrack() {
        // 경로 그림
        val builder: LatLngBounds.Builder = LatLngBounds.Builder() // 카메라 이동을 위한 빌더

        val latLngList = ArrayList<LatLng>()
        for (coordinate in track.gps.coordinates) {
            val latLng = LatLng(coordinate[1], coordinate[0])
            latLngList.add(latLng)
            builder.include(latLng) // 카메라안에 들어와야 하는 지점들 추가
            println("${coordinate[1]}, ${coordinate[0]}")
        }
        mGoogleMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(latLngList)
                .color(ContextCompat.getColor(this, R.color.main_color))
                .width(12F))

        // 체크포인트 추가
        for (checkpointIndex in track.checkPoint) {

            mGoogleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(track.gps.coordinates[checkpointIndex][1], track.gps.coordinates[checkpointIndex][0]))
                    .title("체크포인트")
                    .icon(Utils.getMarkerIconFromDrawable(resources.getDrawable(R.drawable.checkpoint_before,null)))
                    .anchor(0.5F, 0.5F))
        }

        // 출발점 마커 추가
        trackMarkerTextView.text = track.trackName
        mGoogleMap.addMarker(
            MarkerOptions()
                .position(startLatLng)
                .title("출발점")
                .icon(BitmapDescriptorFactory.fromBitmap(Utils.createBitmapFromView(trackMarker)))
                .anchor(0.5F, 0.9F))

        // 카메라 업데이트
        val bounds: LatLngBounds = builder.build()
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
    }
}