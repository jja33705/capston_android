package com.example.capstonandroid.activity

// 내기록 볼때 액티비티

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityMeDetailsBinding
import com.example.capstonandroid.databinding.ActivitySnsdetailsBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.MySNSResponse
import com.example.capstonandroid.network.dto.SNSResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MeDetailsActivity : AppCompatActivity() {

    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    private var page = 1       // 현재 페이지
    lateinit var binding: ActivityMeDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_me_details)

        binding = ActivityMeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRetrofit()
        val data_num : Int = intent.getIntExtra("data_num",0)
        val data_page : Int = intent.getIntExtra("data_page",0)

        val sharedPreference = getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var token = "Bearer " + sharedPreference.getString("TOKEN","")
        println(token)

        supplementService.myIndex(token,data_page-1).enqueue(object : Callback<MySNSResponse> {
            override fun onResponse(call: Call<MySNSResponse>, response: Response<MySNSResponse>) {
                if(response.isSuccessful){

                    println(response.body()!!.data[data_num]!!.title)
                    binding.title.setText(response.body()!!.data[data_num]!!.title)
                    binding.content.setText(response.body()!!.data[data_num]!!.content)


                }  else{
                    println("실패함ㅋㅋ")
                    println(response.body())
                    println(response.message())
                }
            }

            override fun onFailure(call: Call<MySNSResponse>, t: Throwable) {

            }
        })


        binding.backButton.setOnClickListener {
            finish()
        }


    }



    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }


}