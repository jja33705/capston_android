package com.example.capstonandroid.activity

//로그인 액티비티

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.capstonandroid.databinding.ActivityLoginBinding
import com.example.capstonandroid.network.dto.Login
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.dto.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        binding.btnRegister.setOnClickListener {
//            println("hello")
//            val intent : Intent = Intent(this,RegisterActivity::class.java);
//            startActivity(intent);
//        }

        // 함수 초기화
        initRetrofit()



        val nextIntent = Intent(this, MainActivity::class.java)


        val sharedPreference = getSharedPreferences("other", MODE_PRIVATE)

//      이 타입이 디폴트 값
        var token = "Bearer " + sharedPreference.getString("TOKEN","")
        println(token)


        // 변수 만들기

        val registerIntent = Intent(this,RegisterActivity::class.java)

        binding.registerbutton.setOnClickListener {
            println("회원가입 버튼 누름")

            startActivity(registerIntent)
        }






//      로그인 버튼 만들기
        binding.loginButton.setOnClickListener{

//          edittext 이메일 값 받아 오기
            var email = binding.emailEditText.text
//            println(email)

//          edittext 비밀번호 값 받아오기
            var password = binding.passwordEditText.text
//            println(password)

//      객체 만들기
            val login = Login(
                email = email.toString(),
                password = password.toString()
            )


            val nextIntent = Intent(this, MainActivity::class.java)



            supplementService.loginPost(login).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {

                    if(response.isSuccessful){
                        println("성공")
//                  콜백 응답으로 온것
                        println(response.body())

                        nextIntent.putExtra("email",login.email)
                        nextIntent.putExtra("password",login.password)


                        var loginResponse = response.body()

//                        데이터 클래스 USER 사용방법å
//                        var user: User? = loginResponse!!.user
//                        print(user!!.birth)

                        var token: String = loginResponse!!.access_token
                        println(token)

                        val sharedPreference = getSharedPreferences("other", 0)
                        val editor = sharedPreference.edit()
                        editor.putString("TOKEN", token)
                        println("로그인 부분 토큰 값"+ token)
                        editor.apply()



                        startActivity(nextIntent)

                    }else {
                        println("로그인 실패했찌만 "+ token)
                        println("갔지만 실패")
                        println(response.body())
                        println(response.message())
                        println(response.code())
                    }
                }


                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {

                    println("실패")
                    println(t.message)
                }

            })


        }

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

    private fun makeRecyclerView(){

    }

    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }
}