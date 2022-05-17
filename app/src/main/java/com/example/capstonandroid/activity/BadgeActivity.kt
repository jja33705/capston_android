package com.example.capstonandroid.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityBadgeBinding
import com.example.capstonandroid.databinding.ActivityPostBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.LoginUserResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class BadgeActivity : AppCompatActivity() {


    private lateinit var retrofit: Retrofit  //레트로핏
    private lateinit var supplementService: BackendApi // api


    private var _binding: ActivityBadgeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityBadgeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "バッジ"

        initRetrofit()

        val intent = intent

        var token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")

        supplementService.userGet(token).enqueue(object : Callback<LoginUserResponse> {
            override fun onResponse(
                call: Call<LoginUserResponse>,
                response: Response<LoginUserResponse>
            ) {
                if (response.isSuccessful) {


                    if (response.body()!!.Badges.first_exercise == 1) {
                        binding.startExer.setImageResource(R.drawable.start_exer)
                    }
//                    if (response.body()!!.Badges.altitude == true) {
//                        binding.altitude1.setImageResource(R.drawable.altitude1)
//                    }
//                    if (response.body()!!.Badges.altitude2 == true) {
//                        binding.altitude1.setImageResource(R.drawable.altitude2)
//                    }
//                    if (response.body()!!.Badges.altitude3 == true) {
//                        binding.altitude1.setImageResource(R.drawable.altitude3)
//                    }
                    if (response.body()!!.Badges.bike_distance == 1) {
                        binding.bike1.setImageResource(R.drawable.bike1)
                    }
                    if (response.body()!!.Badges.bike_distance2 == 1) {
                        binding.bike2.setImageResource(R.drawable.bike2)
                    }
                    if (response.body()!!.Badges.bike_distance3 == 1) {
                        binding.bike3.setImageResource(R.drawable.bike3)
                    }
                    if (response.body()!!.Badges.run_distance == 1) {
                        binding.bike1.setImageResource(R.drawable.run1)
                    }
                    if (response.body()!!.Badges.run_distance2 == 1) {
                        binding.bike2.setImageResource(R.drawable.run2)
                    }
                    if (response.body()!!.Badges.run_distance3 == 1) {
                        binding.bike3.setImageResource(R.drawable.run3)
                    }
                    if (response.body()!!.Badges.make_track == 1) {
                        binding.bike1.setImageResource(R.drawable.track1)
                    }
                    if (response.body()!!.Badges.make_track2 == 1) {
                        binding.bike2.setImageResource(R.drawable.track2)
                    }
                    if (response.body()!!.Badges.make_track3 == 1) {
                        binding.bike3.setImageResource(R.drawable.track3)
                    }
                }
            }

            override fun onFailure(call: Call<LoginUserResponse>, t: Throwable) {
            }
        })

        }




    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}