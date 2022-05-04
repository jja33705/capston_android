package com.example.capstonandroid.activity

// 앱 켜자마자 뜨는 액티비티

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.capstonandroid.R
import com.example.capstonandroid.service.RecordService
import com.example.capstonandroid.service.TrackPaceMakeService
import com.example.capstonandroid.service.TrackRecordService
import com.example.capstonandroid.databinding.ActivityLoginBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.LoginUserResponse
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

        supportActionBar?.hide()

        var mainIntent = Intent(this,MainActivity::class.java)

//         레코드 중이면 메인 액티비티로 이동
        if (RecordService.isStarted || TrackRecordService.isStarted || TrackPaceMakeService.isStarted) {
            startActivity(mainIntent)
            finish()
        } else {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                initRetrofit()

                val loginIntent = Intent(this, LoginActivity::class.java)

                val bundle = intent.extras
                println("bundle: $bundle")
                println("bundle postId: ${bundle?.getString("postId")}")
                println("bundle type: ${bundle?.getString("type")}")
                println("bundle userId: ${bundle?.getString("id")}")

                // 노티피케이션 눌러서 들어온거면 관련된 곳으로 보내줄 수 있게 넘겨줌
                if (bundle != null) {
                    mainIntent.putExtra("userId", bundle.getString("id"))
                    mainIntent.putExtra("postId", bundle.getString("postId"))
                    mainIntent.putExtra("type", bundle.getString("type"))
                }


                val sharedPreference = getSharedPreferences("other", MODE_PRIVATE)

//      이 타입이 디폴트 값
                var token = "Bearer " + sharedPreference.getString("TOKEN","")
                println("token: $token")

                var autoLogin = sharedPreference.getBoolean("autoLogin", false)
                println("autoLogin : $autoLogin")

                if(autoLogin){
                    supplementService.userGet(token).enqueue(object : Callback<LoginUserResponse> {

                        override fun onResponse(call: Call<LoginUserResponse>, response: Response<LoginUserResponse>) {
                            if (response.isSuccessful) {
                                println(response.body())
                                println("로그인이 되버렸어요")
//                  로그인 토큰 인증이 되면???
                                startActivity(mainIntent)
                                finish()
                            } else {
                                println("로그인이 되지않아요..")
//                  로그인 토큰 인증이 되지않으면?????
                                println(response.message())
                                println(response.body())
                                startActivity(loginIntent)
                                finish()
                            }
                        }
                        override fun onFailure(call: Call<LoginUserResponse>, t: Throwable) {
                            println("아 아예 실패해버렸어요!")
                            startActivity(loginIntent)
                            finish()
                        }
                    })
                } else {
                    startActivity(loginIntent)
                    finish()
                }
            }, 500)
        }
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