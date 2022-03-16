package com.example.capstonandroid.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityLoginBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.LoginUserResponse
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class IntroActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding

    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        initRetrofit()

        val loginIntent = Intent(this,LoginActivity::class.java)

        val mainIntent = Intent(this,MainActivity::class.java)

        val sharedPreference = getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var TOKEN = " 여기는 인트로 입니다. Bearer " + sharedPreference.getString("TOKEN","")
        println(TOKEN)



        supplementService.userGet(TOKEN.toString()).enqueue(object : Callback<LoginUserResponse> {

            override fun onResponse(
                call: Call<LoginUserResponse>,
                response: Response<LoginUserResponse>
            ) {
                if(response.isSuccessful){
//                  로그인 토큰 인증이 되면???
                    startActivity(mainIntent)
                }else {
//                  로그인 토큰 인증이 되지않으면?????
                    startActivity(loginIntent)
                }
            }

            override fun onFailure(call: Call<LoginUserResponse>, t: Throwable) {
                println("아 아예 실패해버렸어요!")
                startActivity(loginIntent)
            }

        })
    }
    override fun onPause() {
        super.onPause()
        finish()
    }
    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }
}