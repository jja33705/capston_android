package com.example.capstonandroid.activity

// 내기록 볼때 액티비티

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType.TYPE_NULL
import android.widget.Toast
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityMeDetailsBinding
import com.example.capstonandroid.databinding.ActivitySnsdetailsBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MeDetailsActivity : AppCompatActivity() {

    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    private var page = 1       // 현재 페이지
    lateinit var binding: ActivityMeDetailsBinding

    private var postID = 0
    private var content :String = ""
    private var range : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_me_details)

        binding = ActivityMeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRetrofit()
        val data_num: Int = intent.getIntExtra("data_num", 0)
        val data_page: Int = intent.getIntExtra("data_page", 0)

        val sharedPreference = getSharedPreferences("other", 0)



//      이 타입이 디폴트 값
        var token = "Bearer " + sharedPreference.getString("TOKEN", "")
        println(token)

        supplementService.myIndex(token, data_page).enqueue(object : Callback<MySNSResponse> {
            override fun onResponse(call: Call<MySNSResponse>, response: Response<MySNSResponse>) {
                if (response.isSuccessful) {

                    println(response.body()!!.data[data_num]!!.title)
                    binding.title.setText(response.body()!!.data[data_num]!!.title)
                    binding.content.setText(response.body()!!.data[data_num]!!.content)

                    postID = response.body()!!.data[data_num].id
                    content = response.body()!!.data[data_num].content.toString()
                    range = response.body()!!.data[data_num].range.toString()

                    println("콘텐츠"+content)
                    println("랭스"+range)
                } else {
                    println("실패함ㅋㅋ")
                    println(response.body())
                    println(response.message())
                }
            }

            override fun onFailure(call: Call<MySNSResponse>, t: Throwable) {

            }
        })
        //      객체 만들기

        if(range=="private"){
            binding.range.setImageResource(R.drawable.lock)}
        else{
            binding.range.setImageResource(R.drawable.lockaa)
        }
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.deleteButton.setOnClickListener {

            println("삭제하기전 페이지?? 뭘까요ㅋㅋ"+postID)
            supplementService.postDelete(token,postID).enqueue(object : Callback<DeleteResponse> {
                override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                    intent.putExtra("resultData","world")
                    setResult(RESULT_OK,intent)
                    finish()
                }
                override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {

                }
            })
        }


        binding.range.setOnClickListener{

            if(range=="public"){
                val update = Update(
                    content = content,
                    range = "private"
                )

                println(update)
                supplementService.postUpdate(token,postID,update).enqueue(object : Callback<UpdateResponse>{
                    override fun onResponse(
                        call: Call<UpdateResponse>,
                        response: Response<UpdateResponse>
                    ) {


                    }

                    override fun onFailure(call: Call<UpdateResponse>, t: Throwable) {

                        println("실패")
                    }

                })
                range = "private"
                binding.range.setImageResource(R.drawable.lock)
            }else {
                val update = Update(
                    content = content,
                    range = "public"
                )

                println(update)
                supplementService.postUpdate(token,postID,update).enqueue(object : Callback<UpdateResponse>{
                    override fun onResponse(
                        call: Call<UpdateResponse>,
                        response: Response<UpdateResponse>
                    ) {


                    }

                    override fun onFailure(call: Call<UpdateResponse>, t: Throwable) {

                        println("실패")
                    }

                })
                range = "public"
                binding.range.setImageResource(R.drawable.lockaa)
            }

        }

        binding.content.setInputType(TYPE_NULL);
        binding.edit.setOnClickListener{
            Toast.makeText(this,"눌렀어요",Toast.LENGTH_SHORT).show()
            println("에딧 눌렀어요!")

        }
    }



        private fun initRetrofit() {
            retrofit = RetrofitClient.getInstance()
            supplementService = retrofit.create(BackendApi::class.java);
        }


}