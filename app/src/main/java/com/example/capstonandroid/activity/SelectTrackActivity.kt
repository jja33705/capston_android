package com.example.capstonandroid.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivitySelectTrackBinding
import com.example.capstonandroid.dto.Track
import com.example.capstonandroid.network.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.*
import retrofit2.Retrofit

class SelectTrackActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    private var _binding: ActivitySelectTrackBinding? = null
    private val binding get() = _binding!!

    private lateinit var mGoogleMap: GoogleMap

    private lateinit var retrofit: Retrofit // 레트로핏 인스턴스
    private lateinit var supplementService: BackendApi // api

    private lateinit var job: Job // 코루틴 동작을 제어하기 위한 job

    private lateinit var tracks: ArrayList<Track> // 트랙 리스트

    private val polylineArray = ArrayList<Polyline>() // 폴리라인 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivitySelectTrackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        job = Job() // job 생성

        initRetrofit() // retrofit 인스턴스 초기화

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // 보이는 지도에 맞게 트랙 가져와 지도에 그려 줌
    private fun drawTracks() {
        CoroutineScope(Dispatchers.Main + job).launch {
            // track 리스트 가져오는 api 호출
            val tracksResponse = supplementService.getTracks("http://13.124.24.179/api/track/search", 128.4642505645752, 35.8402903083385, 128.5250186920166, 128.5250186920166, 16, "B")

            if (tracksResponse.isSuccessful) {
                // 1개 이상 응답이 오면 데이터 보관하고 맵에 그린다.
                if (tracksResponse.body()!!.result.isNotEmpty()) {
                    tracks = tracksResponse.body()!!.result
                    println("응답 옴 $tracks")
                    for ((index, track) in tracks.withIndex()) {
                        val latLngList = ArrayList<LatLng>()

                        for (coordinate in track.gps.coordinates) {
                            latLngList.add(LatLng(coordinate[1], coordinate[0]))
                            println("${coordinate[1]}, ${coordinate[0]}")
                        }

                        val polyline = mGoogleMap.addPolyline(PolylineOptions()
                            .clickable(true)
                            .addAll(latLngList))
                        polyline.width = 5F
                        polyline.color = Color.RED
                        polyline.tag = index
                        polylineArray.add(polyline)
                        println("$index 끝남")
                    }
                } else {
                    tracks.clear()
                    println("${tracksResponse.body()}")
                }
            }

            println("다 그림")
        }

        println("가나다라마바사")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // job 취소
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap.setOnPolylineClickListener(this)

        drawTracks()
    }

    // 레트로핏 초기화
    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java)
    }

    override fun onPolylineClick(polyline: Polyline) {
        binding.slidingLayout.panelHeight = 1000 // 하단 바 올려줌

        binding.trackTitle.text = tracks[polyline.tag as Int].name
        binding.trackDescription.text = tracks[polyline.tag as Int].description
        binding.trackDistance.text = "${tracks[polyline.tag as Int].distance}km"
    }
}