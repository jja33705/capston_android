package com.example.capstonandroid.activity

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstonandroid.*
import com.example.capstonandroid.databinding.ActivityCompleteRecordBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class CompleteRecordActivity : AppCompatActivity() {

    private var _binding: ActivityCompleteRecordBinding? = null
    private val binding: ActivityCompleteRecordBinding get() = _binding!!

    private lateinit var retrofit: Retrofit // 싱글톤 레트로핏 인스턴스
    private lateinit var supplementService: BackendApi // api

    private lateinit var locationList: ArrayList<Location>

    private var time: Int = 0 // 시간

    private var sumAltitude: Double = 0.0 // 누적 상승 고도

    private var distance = 0.0 // 거리

    private var avgSpeed = 0.0 // 평균 속도

    private val altitudes = ArrayList<Int>() // 고도 리스트

    private val speeds = ArrayList<Double>() // 속도 리스트

    private val times = ArrayList<Long>() // 시간 리스트

    private val coordinates = ArrayList<List<Double>>()  //좌표 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCompleteRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRetrofit()

        val intent = intent
        locationList = intent.getParcelableArrayListExtra("locationList")!!
        time = intent.getIntExtra("timeValue", 0)
        println("intent 넘어옴: $time")
        sumAltitude = intent.getDoubleExtra("sumAltitude", 0.0)
        println("intent 넘어옴: $sumAltitude")
        distance = intent.getDoubleExtra("distance", 0.0)
        println("intent 넘어옴: $distance")
        avgSpeed = intent.getDoubleExtra("avgSpeed", 0.0)
        println("intent 넘어옴: $avgSpeed")





        println(locationList.size)

        for (location in locationList) {
            altitudes.add(location.altitude.toInt())
            speeds.add(location.speed.toDouble())
            times.add(location.time)
            coordinates.add(listOf(location.latitude, location.longitude))
        }

        binding.btPost.setOnClickListener{
//            supplementService.test("abcdefg").enqueue(object : Callback<Test> {
//                override fun onResponse(call: Call<Test>, response: Response<Test>) {
//                    if (response.isSuccessful) {
//                        println("성공")
//                        println(response.body()?.test)
//                    }
//                }
//
//                override fun onFailure(call: Call<Test>, t: Throwable) {
//                    println("실패${t.toString()}")
//                }
//
//            })

            val record: Record = Record(sumAltitude.toInt(), altitudes, avgSpeed, 99.99, coordinates, distance, "자전거", "public", speeds, time, times)
            supplementService.storePost(record).enqueue(object : Callback<Record> {
                override fun onResponse(call: Call<Record>, response: Response<Record>) {

                    println(response.code())
                    if (response.isSuccessful) {
                        println("성공")
                        println(response.body())
                    } else {
                        println("갔는데 망함")
                    }
                }

                override fun onFailure(call: Call<Record>, t: Throwable) {
                    println("실패")
                    println(t.message)
                }
            })
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