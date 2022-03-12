package com.example.capstonandroid.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityMainBinding
import com.example.capstonandroid.fragment.HomeFragment
import com.example.capstonandroid.fragment.MeFragment
import com.example.capstonandroid.fragment.TrackFragment
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding!!


    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//      함수 초기화
        initRetrofit()

        val sharedPreference = getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var TOKEN = "Bearer " + sharedPreference.getString("TOKEN","")
        println(TOKEN)


            supplementService.userGet(TOKEN.toString()).enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {

                    if(response.isSuccessful){
                        println("성공 프래그먼트")
//                  콜백 응답으로 온것
                        println(response.body())


                    }else {
                        println("갔지만 실패")
                        println(response.body())
                        println(response.message())
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {

                }

            })


//      토큰 불러오기




//        var email = intent.getStringExtra("email").toString()
//        var password = intent.getStringExtra("password").toString()
//
//        println(email)
//        println(password)
//
//        val login = Login(
//            email = email,
//            password = password
//        )
//        supplementService.loginPost(login).enqueue(object : Callback<LoginResponse> {
//            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
//
//                if(response.isSuccessful){
//                    println("성공 프래그먼트")
////                  콜백 응답으로 온것
//                    println(response.body())
//
//
//                }else {
//                    println("갔지만 실패")
//                    println(response.body())
//                    println(response.message())
//                    println(response.code())
//                }
//            }
//
//
//            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
//
//                println("실패")
//                println(t.message)
//            }
//
//        })
        // 바텀 네비게이션에서 선택한 메뉴 아이디에 따라 표시할 화면 분기처리 (나중에 addToBackStack 부분 찾아보고 Transaction 관리해 줘야 할 것 같음.)
        binding.bottomNav.setOnItemSelectedListener {
            println(it.itemId)
            when (it.itemId) {
                R.id.homeFragment -> {

                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
                }
                R.id.trackFragment -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, TrackFragment()).commit()
                }
                R.id.recordActivity -> {
                    val intent: Intent = Intent(this, RecordActivity::class.java)
                    startActivity(intent)
                    return@setOnItemSelectedListener false
                }
                R.id.meFragment -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MeFragment()).commit()
                }
            }
            true
        }

        // 처음 들어왔을때는 homeFragment
        binding.bottomNav.selectedItemId = R.id.homeFragment



    }


    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
