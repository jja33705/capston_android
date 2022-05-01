package com.example.capstonandroid.activity

//로그인 액티비티

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.capstonandroid.databinding.ActivityLoginBinding
import com.example.capstonandroid.network.dto.Login
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.dto.FcmToken
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit


class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    private lateinit var token: String
    private lateinit var fcmToken: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        initRetrofit()

        // 변수 만들기
        val registerIntent = Intent(this,RegisterActivity::class.java)

        binding.registerbutton.setOnClickListener {
            println("회원가입 버튼 누름")

            startActivity(registerIntent)
        }

        binding.forgotEmail.setOnClickListener {

        }

        binding.loginButton.setOnClickListener{
            CoroutineScope(Dispatchers.Main).launch {
                val login = Login(binding.emailEditText.text.toString(), binding.passwordEditText.text.toString())
                val loginResponse = supplementService.loginPost(login)
                if (loginResponse.isSuccessful) {
                    println("로그인 성공")

                    token = loginResponse.body()!!.access_token
                    println(token)

                    // fcm 토큰 가져와서 바뀌었는지 아닌지 확인하고 로그인 처리
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        CoroutineScope(Dispatchers.Main).launch {
                            if (task.isSuccessful) { // fcm 토큰 가져오기 실패했을 때
                                fcmToken = task.result
                                println("get fcmToken: $fcmToken")

                                // fcm 토큰 바뀌었으면 갱신해줌
                                if (fcmToken != getSharedPreferences("other", MODE_PRIVATE).getString("fcmToken", "")) {
                                    println("fcm token 다름")

                                    val fcmTokenResponse = supplementService.fcmToken("Bearer $token", FcmToken(fcmToken))
                                    if (fcmTokenResponse.isSuccessful) {
                                        println("백엔드에 fcmToken 갱신: ${fcmTokenResponse.body()!!.message}")
                                        saveSharedPreferences()
                                        loginIntent()
                                    } else { // 실패했을 때

                                    }
                                } else {
                                    println("fcm token 같음")
                                    saveSharedPreferences()
                                    loginIntent()
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    private fun loginIntent() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveSharedPreferences() {
        val sharedPreferences = getSharedPreferences("other", MODE_PRIVATE)
        val sharedPreferencesEdit = sharedPreferences.edit()
        sharedPreferencesEdit.putString("TOKEN", token)
        sharedPreferencesEdit.putBoolean("autoLogin", binding.autoLoginCheckBox.isChecked)
        sharedPreferencesEdit.putString("fcmToken", fcmToken)
        sharedPreferencesEdit.commit()
    }

    override fun onStart() {
        super.onStart()
    }


//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        startActivity(Intent(this, AuthActivity::class.java))
//        return super.onOptionsItemSelected(item)
//    }

    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }
}