package com.example.capstonandroid.activity

// SNS 누르면 자세히 뜨는 것

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.*
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.SNSResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class SNSDetailsActivity : AppCompatActivity() {
    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    private var page = 0       // 현재 페이지


    lateinit var binding: ActivitySnsdetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snsdetails)


        binding = ActivitySnsdetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRetrofit()

//        if (intent.hasExtra("indexnumber")) {
//            binding.textView10.text = intent.getStringExtra("indexnumber")
//            /* "nameKey"라는 이름의 key에 저장된 값이 있다면
//               textView의 내용을 "nameKey" key에서 꺼내온 값으로 바꾼다 */
//
//        } else {
//            Toast.makeText(this, "전달된 이름이 없습니다", Toast.LENGTH_SHORT).show()
//        }
//      누른 indexnumber~ 받아오기

        val data_num : Int = intent.getIntExtra("data_num",0)
        val data_page : Int = intent.getIntExtra("data_page",0)

        println(data_num.toString())
        println(data_page.toString())
        val sharedPreference = getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var TOKEN = "Bearer " + sharedPreference.getString("TOKEN","")
        println(TOKEN)


         supplementService.SNSIndex(TOKEN,data_page-1).enqueue(object : Callback<SNSResponse> {
             override fun onResponse(call: Call<SNSResponse>, response: Response<SNSResponse>) {

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

             override fun onFailure(call: Call<SNSResponse>, t: Throwable) {
                 println("아예 가지도 않음ㅋㅋ")
                 println(t.message)
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