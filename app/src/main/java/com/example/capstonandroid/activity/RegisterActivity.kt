package com.example.capstonandroid.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstonandroid.databinding.ActivityRegisterBinding
import com.example.capstonandroid.dto.Register
import com.example.capstonandroid.dto.RegisterResponse
import com.example.capstonandroid.network.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class RegisterActivity : AppCompatActivity() {


    private var _binding: ActivityRegisterBinding? = null
    private val binding: ActivityRegisterBinding get() = _binding!!

    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    override fun onCreate(savedInstanceState: Bundle?) {

        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initRetrofit()

        val registerIntent = Intent(this,LoginActivity::class.java)
//

//      뒤로가기
        binding.backButton.setOnClickListener {
            finish()
        }


        binding.btnRegister.setOnClickListener {
            //        edittext 이름 값 받아 오기
            var name : String  = binding.etRegisterName.text.toString()
//            println(email)

//          edittext 이메일 값 받아오기
            var email : String= binding.etRegisterEmail.text.toString()
//            println(password)

            var sex :String = binding.etRegisterSex.text.toString()

            var password:String = binding.etRegisterPassword.text.toString()


            var weight:String = binding.etRegisterWeight.text.toString()

            var profile:String = binding.etRegisterProfile.text.toString()

            var birth:String = binding.etRegisterBirth.text.toString()

            var introduce:String = binding.etRegisterIntroduce.text.toString()

            var location :String= binding.etRegisterLocation.text.toString()

            val register = Register(
                name = name.toString(),
                email = email.toString(),
                password = password.toString(),
                sex = sex.toString(),
                weight = weight.toString(),
                profile = profile.toString(),
                birth = birth.toString(),
                introduce = introduce.toString(),
                location = location.toString()
            )

            println(register)

            supplementService.registerPost(register).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {
                    if(response.isSuccessful){
                        println("성공")
//                  콜백 응답으로 온것
                        println(response.body())

                        finish()
                    }
                    else {
                        println("갔지만 실패")
                        println(response.body())
                        println(response.message())
                        println(response.code())
                    }


                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {

                    println("실패")
                    println(t.message)
                }


            })
        }


    }



    override fun onStart() {
        super.onStart()
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