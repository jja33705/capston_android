package com.example.capstonandroid.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.capstonandroid.BadgeDialog
import com.example.capstonandroid.GoalDialog
import com.example.capstonandroid.MyApplication
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


        supplementService.getBadges(token).enqueue(object :Callback<Int>{
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                println("ok")
            }
            override fun onFailure(call: Call<Int>, t: Throwable) {
            }
        })


        supplementService.userGet(token).enqueue(object : Callback<LoginUserResponse> {
            override fun onResponse(
                call: Call<LoginUserResponse>,
                response: Response<LoginUserResponse>
            ) {
                if (response.isSuccessful) {

                    println("userP " +response.body()!!.badges)



                    if (response.body()?.badges?.first_exercise == 1) {
                        binding.startExer.setImageResource(R.drawable.start_exer)
                    }
                    if (response.body()?.badges?.altitude == 1) {
                        binding.altitude1.setImageResource(R.drawable.altitude1)
                    }
                    if (response.body()?.badges?.altitude2 == 1) {
                        binding.altitude1.setImageResource(R.drawable.altitude2)
                    }
                    if (response.body()?.badges?.altitude3 == 1) {
                        binding.altitude1.setImageResource(R.drawable.altitude3)
                    }
                    if (response.body()?.badges?.bike_distance == 1) {
                        binding.bike1.setImageResource(R.drawable.bike1)
                    }
                    if (response.body()?.badges?.bike_distance2 == 1) {
                        binding.bike2.setImageResource(R.drawable.bike2)
                    }
                    if (response.body()?.badges?.bike_distance3 ==1) {
                        binding.bike3.setImageResource(R.drawable.bike3)
                    }
                    if (response.body()?.badges?.run_distance == 1) {
                        binding.running1.setImageResource(R.drawable.run1)
                    }
                    if (response.body()?.badges?.run_distance2 == 1) {
                        binding.running2.setImageResource(R.drawable.run2)
                    }
                    if (response.body()?.badges?.run_distance3 == 1) {
                        binding.running3.setImageResource(R.drawable.run3)
                    }
                    if (response.body()?.badges?.make_track == 1) {
                        binding.track1.setImageResource(R.drawable.track1)
                    }
                    if (response.body()?.badges?.make_track2 ==1) {
                        binding.track2.setImageResource(R.drawable.track2)
                    }
                    if (response.body()?.badges?.make_track3 == 1) {
                        binding.track3.setImageResource(R.drawable.track3)
                    }
                }
            }

            override fun onFailure(call: Call<LoginUserResponse>, t: Throwable) {
            }
        })



        binding.startExer.setOnClickListener{
            MyApplication.prefs.setString("badgeType", "0")
            MyApplication.prefs.setString("TOKEN",token)
            showBadgeDialog()
        }
        binding.altitude1.setOnClickListener{
            MyApplication.prefs.setString("badgeType", "1")
            MyApplication.prefs.setString("TOKEN",token)
            showBadgeDialog()
        }
        binding.altitude2.setOnClickListener{

            MyApplication.prefs.setString("badgeType", "2")
            MyApplication.prefs.setString("TOKEN",token)
            showBadgeDialog()
        }
        binding.altitude3.setOnClickListener {


            MyApplication.prefs.setString("badgeType", "3")
            MyApplication.prefs.setString("TOKEN",token)
            showBadgeDialog()
        }
        binding.bike1.setOnClickListener {

            MyApplication.prefs.setString("badgeType", "4")
            MyApplication.prefs.setString("TOKEN",token)
            showBadgeDialog()
        }
        binding.bike2.setOnClickListener {

            MyApplication.prefs.setString("badgeType", "5")
            MyApplication.prefs.setString("TOKEN",token)
            showBadgeDialog()
        }
        binding.bike3.setOnClickListener {
            MyApplication.prefs.setString("badgeType", "6")
            MyApplication.prefs.setString("TOKEN",token)
            showBadgeDialog()
        }
        binding.running1.setOnClickListener {
            MyApplication.prefs.setString("badgeType", "7")
            MyApplication.prefs.setString("TOKEN",token)

            showBadgeDialog()
        }
        binding.running2.setOnClickListener {
            MyApplication.prefs.setString("badgeType", "8")
            MyApplication.prefs.setString("TOKEN",token)

            showBadgeDialog()
        }
        binding.running3.setOnClickListener {
            MyApplication.prefs.setString("badgeType", "9")
            MyApplication.prefs.setString("TOKEN",token)

            showBadgeDialog()
        }
        binding.track1.setOnClickListener {
            MyApplication.prefs.setString("badgeType", "10")
            MyApplication.prefs.setString("TOKEN",token)

            showBadgeDialog()
        }
        binding.track2.setOnClickListener {
            MyApplication.prefs.setString("badgeType", "11")
            MyApplication.prefs.setString("TOKEN",token)

            showBadgeDialog()
        }
        binding.track3.setOnClickListener {
            MyApplication.prefs.setString("badgeType", "12")
            MyApplication.prefs.setString("TOKEN",token)

            showBadgeDialog()
        }
    }

    private fun showBadgeDialog() {
        BadgeDialog(this) {
        }.show()
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