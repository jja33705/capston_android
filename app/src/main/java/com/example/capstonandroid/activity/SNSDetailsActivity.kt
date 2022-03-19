package com.example.capstonandroid.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityLoginBinding
import com.example.capstonandroid.databinding.ActivitySnsdetailsBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import retrofit2.Retrofit

class SNSDetailsActivity : AppCompatActivity() {
    lateinit var binding: ActivitySnsdetailsBinding
    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snsdetails)

        initRetrofit()
        val nextIntent = intent.getStringExtra("number")


        val sharedPreference = getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var TOKEN = "Bearer " + sharedPreference.getString("TOKEN","")
        println(TOKEN)

        println(nextIntent.toString())

    }
    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }
}