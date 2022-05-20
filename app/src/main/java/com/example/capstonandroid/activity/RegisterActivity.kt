package com.example.capstonandroid.activity

// 회원가입 액티비티

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityRegisterBinding
import com.example.capstonandroid.network.dto.Register
import com.example.capstonandroid.network.dto.RegisterResponse
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.text.DecimalFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {


    private var _binding: ActivityRegisterBinding? = null
    private val binding: ActivityRegisterBinding get() = _binding!!

    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api


    private val sexList = arrayOf("男性", "女性", "トランスジェンダー", "知らせたくない")
    private val sexValueList = arrayOf("M", "F", "T", "N")
    private var selectedSexIndex = 0

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
        binding.etRegisterSex.setOnClickListener {
            AlertDialog.Builder(this).setTitle("性別").setSingleChoiceItems(sexList, selectedSexIndex) { dialogInterface, position ->
                selectedSexIndex = position
                binding.etRegisterSex.setText(sexList[selectedSexIndex])
                dialogInterface.cancel()
            }.show()
        }
        binding.etRegisterBirth.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                val decimalFormat = DecimalFormat("00")
                binding.etRegisterBirth.setText("$year-${decimalFormat.format(month + 1)}-${decimalFormat.format(day)}")
            }
            val datePickerDialog = DatePickerDialog(this, R.style.DatePickerDialog_Spinner, dateSetListener, calendar.get(
                Calendar.YEAR), calendar.get(Calendar.MONDAY), calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
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

            var birth:String = binding.etRegisterBirth.text.toString()

            var introduce:String = binding.etRegisterIntroduce.text.toString()

            var location :String= binding.etRegisterLocation.text.toString()

            val register = Register(
                name = name.toString(),
                email = email.toString(),
                password = password.toString(),
                sex = sexValueList[selectedSexIndex],
                weight = weight.toString(),
                profile = "https://run-images.s3.ap-northeast-2.amazonaws.com/default-profile/KakaoTalk_Photo_2022-04-17-14-55-41.png",
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