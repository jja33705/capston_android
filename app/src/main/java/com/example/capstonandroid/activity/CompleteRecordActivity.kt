package com.example.capstonandroid.activity

// 레코드 완료 후 뜨는 액티비티

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.room.Room
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityCompleteRecordBinding
import com.example.capstonandroid.db.AppDatabase
import com.example.capstonandroid.db.dao.GpsDataDao
import com.example.capstonandroid.db.entity.GpsData
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.dto.PostRecordActivity
import com.example.capstonandroid.network.dto.PostRecordGpsData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class CompleteRecordActivity : AppCompatActivity() {

    private var _binding: ActivityCompleteRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var retrofit: Retrofit // 레트로핏 인스턴스
    private lateinit var supplementService: BackendApi // api

    private lateinit var gpsDataDao: GpsDataDao // db dao 핸들

    private lateinit var gpsDataList: List<GpsData>

    // postRecordGpsData 에 넣을 하나하나의 리스트들
    private lateinit var speedList: ArrayList<Float>
    private lateinit var gpsList: ArrayList<List<Double>>
    private lateinit var altitudeList: ArrayList<Double>
    private lateinit var distanceList: ArrayList<Double>
    private lateinit var timeList: ArrayList<Int>

    private lateinit var postRecordGpsData: PostRecordGpsData // 총 합친 gps 데이터

    private lateinit var range: String // 공개 범위

    private lateinit var matchType: String // 매치 타입

    private lateinit var exerciseKind: String // 운동 종류

    private var second: Int = 0 // 시간

    private var sumAltitude = 0.0 // 누적 상승 고도

    private var distance = 0.0 // 거리

    private var avgSpeed = 0.0 // 평균 속도

    private var kcal = 0.0 // 칼로리

    private var trackId: String? = null // 트랙 아이디

    private var loadedGpsData = false // gps 데이터 로딩 완료했는지

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCompleteRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRetrofit()

        supportActionBar?.title = "활동 업로드" // 액션바 텍스트

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database").build()
        gpsDataDao = db.gpsDataDao()

        speedList = ArrayList()
        gpsList = ArrayList()
        altitudeList = ArrayList()
        distanceList = ArrayList()
        timeList = ArrayList()

        // db에 저장돼 있는 gps 데이터 불러옴
        CoroutineScope(Dispatchers.IO).launch {
            gpsDataList = gpsDataDao.getAll()

            for (gpsData in gpsDataList) {
                speedList.add(gpsData.speed)
                gpsList.add(listOf(gpsData.lng, gpsData.lat))
                altitudeList.add(gpsData.altitude)
                distanceList.add(gpsData.distance)
                timeList.add(gpsData.second)
            }

            postRecordGpsData = PostRecordGpsData(speedList, gpsList, altitudeList, distanceList, timeList)

            loadedGpsData = true
        }


        val intent = intent
        second = intent.getIntExtra("second", 0)
        println("intent 넘어옴 (second): $second")
        sumAltitude = intent.getDoubleExtra("sumAltitude", 0.0)
        println("intent 넘어옴 (sumAltitude): $sumAltitude")
        distance = intent.getDoubleExtra("distance", 0.0)
        println("intent 넘어옴 (distance): $distance")
        avgSpeed = intent.getDoubleExtra("avgSpeed", 0.0)
        println("intent 넘어옴 (avgSpeed): $avgSpeed")
        kcal = intent.getDoubleExtra("kcal", 0.0)
        println("intent 넘어옴 (kcal): $kcal")
        trackId = intent.getStringExtra("trackId")
        println("intent 넘어옴 (trackId): $trackId")
        matchType = intent.getStringExtra("matchType")!!
        println("intent 넘어옴 (matchType): $matchType")
        exerciseKind = intent.getStringExtra("exerciseKind")!!
        println("intent 넘어옴 (exerciseKind): $exerciseKind")



        // 스피너 설정
        binding.spinnerRange.adapter = ArrayAdapter.createFromResource(this, R.array.range, android.R.layout.simple_spinner_item)
        binding.spinnerRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        range = "public"
                    }
                    1 -> {
                        range = "private"
                    }
                }
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {
            }
        }

        // 등록 버튼 눌렀을 때
        binding.btPost.setOnClickListener{

            // db에 있는 gps 데이터 다 로딩했는지에 따라 분기처리
            if (loadedGpsData) {
                CoroutineScope(Dispatchers.Main).launch {
                    val token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!
                    val postActivityResponse = supplementService.postRecordActivity(token, PostRecordActivity(sumAltitude, avgSpeed, kcal, binding.etDescription.text.toString(), distance, exerciseKind, "혼자하기", range, second, binding.etTitle.text.toString(), trackId, postRecordGpsData))

                    println(postActivityResponse.code())

                    if (postActivityResponse.isSuccessful) {
                        println(postActivityResponse.body())
                        Toast.makeText(this@CompleteRecordActivity, "저장 성공", Toast.LENGTH_SHORT)
                        finish()
                    } else {
                        println(postActivityResponse.message())
                        println(postActivityResponse.errorBody())
                        println(postActivityResponse.toString())
                        Toast.makeText(this@CompleteRecordActivity, "저장 실패", Toast.LENGTH_SHORT)
                    }
                }
            } else {
                Toast.makeText(this@CompleteRecordActivity, "활동 데이터 로드 중. 잠시 후 다시 시도하세요", Toast.LENGTH_SHORT)
            }
        }
    }

    // 레트로핏 초기화
    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}