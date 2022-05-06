package com.example.capstonandroid.activity

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capstonandroid.adapter.RecyclerFollowerAdapter
import com.example.capstonandroid.databinding.ActivityFollowerBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class FollowerActivity : AppCompatActivity() {

    private var _binding: ActivityFollowerBinding? = null
    private val binding get() = _binding!!

    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityFollowerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRetrofit()

    }
    override fun onStart() {
        super.onStart()
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        val sharedPreference = getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var token = "Bearer " + sharedPreference.getString("TOKEN","")


        binding.successButton.setOnClickListener {
            Toast.makeText(this, binding.editText.text.toString(), Toast.LENGTH_SHORT).show()
            binding.followerRecyclerview.visibility = View.VISIBLE

            val list = ArrayList<FollowerData>()
            val adapter = RecyclerFollowerAdapter(list, { data -> adapterOnClick(data) })

                supplementService.userSearch(token,binding.editText.text.toString()).enqueue(object :
                    Callback<FollowerResponse> {
                    override fun onResponse(
                        call: Call<FollowerResponse>,
                        response: Response<FollowerResponse>
                    ) {

                        if(binding.editText.text.toString()==""){

                        }else{
                        if(response.body()!!.data.size==0){
                            println("아무것도없다")

                            list.clear()

                            binding.followerRecyclerview.adapter = adapter

                        }else {
                            for (i in 0..response.body()!!.data.size-1) {
                                list.add(
                                    FollowerData(
                                        response.body()!!.data[i].name,
                                        response.body()!!.data[i].profile,
                                        response.body()!!.data[i].name,
                                        response.body()!!.data[i].id,
                                        response.body()!!.data[i].followCheck
                                    )
                                )
                            }
                            binding.followerRecyclerview.adapter = adapter
                        }}
                    }

                    override fun onFailure(call: Call<FollowerResponse>, t: Throwable) {
                        println("아 아니야")
                    }
                })



        }


    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    private fun adapterOnClick(data: FollowerData) {
//        Toast.makeText(requireContext(), "FunCall Clicked -> ID : ${data.title}, Name : ${data.name}", Toast.LENGTH_SHORT).show()
//        println(data.data_num)


        val sharedPreference = getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var token = "Bearer " + sharedPreference.getString("TOKEN","")


//        supplementService.userFollow(token,data.id).enqueue(object : Callback<FollowResponse>{
//            override fun onResponse(
//                call: Call<FollowResponse>,
//                response: Response<FollowResponse>
//            ) {
//                println("성공 했습니다")
//
//                val list = ArrayList<FollowerData>()
//                val adapter = RecyclerFollowerAdapter(list, { data -> adapterOnClick(data) })
//
//                if(!(binding.editText.text.toString()==="")){
//                    var text = binding.editText.text.toString()
//
//                    supplementService.userSearch(token,binding.editText.text.toString()).enqueue(object :
//                        Callback<FollowerResponse> {
//                        override fun onResponse(
//                            call: Call<FollowerResponse>,
//                            response: Response<FollowerResponse>
//                        ) {
//
//                            println(response.body()!!)
//
//                            if(response.body()!!.data.size==0){
//                                println("아무것도없다")
//
//                                list.clear()
//
//                                binding.followerRecyclerview.adapter = adapter
//
//                            }else {
//                                for (i in 0..response.body()!!.data.size-1) {
//                                    list.add(
//                                        FollowerData(
//                                            response.body()!!.data[i].name,
//                                            response.body()!!.data[i].profile,
//                                            response.body()!!.data[i].name,
//                                            response.body()!!.data[i].id,
//                                            response.body()!!.data[i].followCheck
//                                        )
//                                    )
//                                }
//                                binding.followerRecyclerview.adapter = adapter
//                            }
//                        }
//
//                        override fun onFailure(call: Call<FollowerResponse>, t: Throwable) {
//                            println("아 아니야")
//                        }
//                    })
//                }
//
//            }
//
//            override fun onFailure(call: Call<FollowResponse>, t: Throwable) {
//                println("실패 했습니다")
//            }
//
//            })
//
//

    }
    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }
}