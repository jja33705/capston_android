package com.example.capstonandroid.activity

// 레코드 완료 후 뜨는 액티비티

import android.location.Location
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
import com.example.capstonandroid.network.dto.Record
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.dto.PostRecordActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit



class CompleteRecordActivity : AppCompatActivity() {

    private var _binding: ActivityCompleteRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var retrofit: Retrofit // 레트로핏 인스턴스
    private lateinit var supplementService: BackendApi // api

    private lateinit var exerciseKind: String // 운동 종류

    private lateinit var gpsDataDao: GpsDataDao // db dao 핸들

    private lateinit var gpsDataList: List<GpsData>

    private var second: Int = 0 // 시간

    private var sumAltitude: Int = 0 // 누적 상승 고도

    private var distance = 0.0 // 거리

    private var avgSpeed = 0.0 // 평균 속도

    private var kcal = 0 // 칼로리

    private var range = "public" // 공개범위

    private var loadedGpsData = false // gps 데이터 로딩 완료했는지

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCompleteRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRetrofit()

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database").build()
        gpsDataDao = db.gpsDataDao()

        // db에 저장돼 있는 gps 데이터 불러옴
        CoroutineScope(Dispatchers.IO).launch {
            gpsDataList = gpsDataDao.getAll()
            loadedGpsData = true
        }


        val intent = intent
        second = intent.getIntExtra("second", 0)
        println("intent 넘어옴: $second")
        sumAltitude = intent.getDoubleExtra("sumAltitude", 0.0).toInt()
        println("intent 넘어옴: $sumAltitude")
        distance = intent.getDoubleExtra("distance", 0.0)
        println("intent 넘어옴: $distance")
        avgSpeed = intent.getDoubleExtra("avgSpeed", 0.0)
        println("intent 넘어옴: $avgSpeed")
        kcal = intent.getIntExtra("kcal", 0)
        println("intent 넘어옴: $kcal")
        exerciseKind = getSharedPreferences("record", MODE_PRIVATE).getString("exerciseKind", "")!!
        println(exerciseKind)

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

        binding.btPost.setOnClickListener{
            if (loadedGpsData) {
                CoroutineScope(Dispatchers.Main).launch {
                    val postActivityResponse = supplementService.postRecordActivity("Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!, PostRecordActivity(sumAltitude, avgSpeed, kcal, binding.etDescription.text.toString(), distance, getSharedPreferences("trackRecord", MODE_PRIVATE).getString("exerciseKind", "")!!, "혼자하기", range, second, binding.etTitle.text.toString(), null, gpsDataList))

                    println("Bearer ${getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!}")
                    println(gpsDataList)

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