package com.example.capstonandroid

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Color.rgb
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.capstonandroid.databinding.BadgeDialogBinding
import com.example.capstonandroid.databinding.GoalAlertDialogBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.Goal
import com.example.capstonandroid.network.dto.GoalResponse
import com.example.capstonandroid.network.dto.LoginUserResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.*

class BadgeDialog(
    context: Context,
    private val okCallback: (String) -> Unit,
) : Dialog(context) { // 뷰를 띄워야하므로 Dialog 클래스는 context를 인자로 받는다.





    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    private lateinit var binding: BadgeDialogBinding

    private var badge : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 만들어놓은 dialog_profile.xml 뷰를 띄운다.
        binding = BadgeDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRetrofit()
        initViews()

    }

    private fun initViews() = with(binding) {
        // 뒤로가기 버튼, 빈 화면 터치를 통해 dialog가 사라지지 않도록
        setCancelable(false)

        // background를 투명하게 만듦
        // (중요) Dialog는 내부적으로 뒤에 흰 사각형 배경이 존재하므로, 배경을 투명하게 만들지 않으면
        // corner radius의 적용이 보이지 않는다.

        // OK Button 클릭에 대한 Callback 처리. 이 부분은 상황에 따라 자유롭게!

        var btn_0 : Int = 0
       var btn_1 : Int = 0
         var btn_2 : Int = 0
        var btn_3 : Int = 0
         var btn_4 : Int = 0
         var btn_5 : Int = 0
         var btn_6 : Int = 0
         var btn_7 : Int = 0
         var btn_8 : Int = 0
        var btn_9 : Int = 0
         var btn_10 : Int = 0
         var btn_11 : Int = 0
         var btn_12 : Int = 0
        var token = "Bearer "+MyApplication.prefs.getString("TOKEN", "")

        var badgeType = MyApplication.prefs.getString("badgeType", "")
        if(badgeType=="0"){
            badge = "first_exercise"
        }else if (badgeType=="1"){
            badge = "altitude"
        }else if (badgeType=="2"){
            badge = "altitude2"
        }else if (badgeType=="3"){
            badge = "altitude3"
        }else if (badgeType=="4"){
            badge = "bike_distance"
        }else if (badgeType=="5"){
            badge = "bike_distance2"
        }else if (badgeType=="6"){
            badge = "bike_distance3"
        }else if (badgeType=="7"){
            badge = "run_distance"
        }else if (badgeType=="8"){
            badge = "run_distance2"
        }else if (badgeType=="9"){
            badge = "run_distance3"
        }else if (badgeType=="10"){
            badge = "make_track"
        }else if (badgeType=="11"){
            badge = "make_track"
        }else if (badgeType=="12"){
            badge = "make_track"
        }

        supplementService.userGet(token).enqueue(object : Callback<LoginUserResponse> {
            override fun onResponse(
                call: Call<LoginUserResponse>,
                response: Response<LoginUserResponse>
            ) {
                btn_0 = response.body()!!.badges.first_exercise
                btn_1 = response.body()!!.badges.altitude
                btn_2 = response.body()!!.badges.altitude2
                btn_3 = response.body()!!.badges.altitude3
                btn_4 = response.body()!!.badges.bike_distance
                btn_5 = response.body()!!.badges.bike_distance2
                btn_6 = response.body()!!.badges.bike_distance3
                btn_7 = response.body()!!.badges.run_distance
                btn_8 = response.body()!!.badges.run_distance2
                btn_9 = response.body()!!.badges.run_distance3
                btn_10 = response.body()!!.badges.make_track
                btn_11 = response.body()!!.badges.make_track2
                btn_12 = response.body()!!.badges.make_track3

                if(badgeType=="0"){
                    binding.title.text = "新しい出発"
                    binding.content.text = "走りましょう！"
                    binding.number.visibility = View.GONE
                    binding.badgeImage.setImageResource(R.drawable.start_exer)
                    if(btn_0==0){
                        binding.set.isEnabled = false
                        binding.set.text = "未取得"
                        binding.badgeImage.setImageResource(R.drawable.start_g)
                    }
                }
                else if(badgeType=="1"||badgeType=="2"||badgeType=="3"){
//            supplementService.totalAltitude(token).enqueue(object : Callback<Int>{
//                override fun onResponse(call: Call<Int>, response: Response<Int>) {
//                    if (response.isSuccessful) {
//                        myAltitude = response.body()!!.toInt()
//                    }
//                }
//                override fun onFailure(call: Call<Int>, t: Throwable) {
//                }
//            })
                    var myAltitude : Double = 0.0
                    CoroutineScope(Dispatchers.Main).launch {
                        val altitudeResponse = supplementService.totalAltitude(token)
                        if(altitudeResponse.isSuccessful){
                            myAltitude = altitudeResponse.body()!!
                            if(badgeType == "1"){
                                binding.title.text = "高度初心者"
                                binding.content.text = "どんどん慣れますよ"
                                binding.number.text = "${myAltitude}m / 10000m"
                                binding.badgeImage.setImageResource(R.drawable.altitude1)
                                if(btn_1==0){
                                    binding.badgeImage.setImageResource(R.drawable.altitude1_g)
                                    binding.set.isEnabled = false
                                    binding.set.text = "未取得"
                                }
                            }else if (badgeType == "2"){
                                binding.title.text = "高度中級者"
                                binding.content.text = "結構慣れましたね！"
                                binding.number.text = "${myAltitude}m / 20000m"
                                binding.badgeImage.setImageResource(R.drawable.altitude2)
                                if(btn_2==0){
                                    binding.badgeImage.setImageResource(R.drawable.altitude2_g)
                                    binding.set.isEnabled = false
                                    binding.set.text = "未取得"
                                }
                            }else if (badgeType == "3"){
                                binding.title.text = "高度マスター"
                                binding.content.text = "高度マスターになりました！"
                                binding.number.text = "${myAltitude}m / 30000m"
                                binding.badgeImage.setImageResource(R.drawable.altitude3)
                                if(btn_3==0){
                                    binding.badgeImage.setImageResource(R.drawable.altitude3_g)
                                    binding.set.isEnabled = false
                                    binding.set.text = "未取得"
                                }
                            }
                        }
                    }



                }else if (badgeType=="4"||badgeType=="5"||badgeType=="6"){
                    CoroutineScope(Dispatchers.Main).launch {
                        val distanceBikeResponse = supplementService.totalDistance(token, "B")
                        if(distanceBikeResponse.isSuccessful){

                            var myBikeDistance : Double = 0.0
                            if(distanceBikeResponse.isSuccessful){
                                when (distanceBikeResponse.code()) {
                                    200 -> {
                                        myBikeDistance = distanceBikeResponse.body()!!.distance.toDouble()
                                    }
                                }
                            }
                            if(badgeType == "4"){
                                binding.title.text = "サイクリング初心者"
                                binding.content.text = "どんどん慣れますよ"
                                binding.number.text = "${myBikeDistance}km / 1000km"
                                binding.badgeImage.setImageResource(R.drawable.bike1)
                                if(btn_4==0){
                                    binding.badgeImage.setImageResource(R.drawable.bike1_g)
                                    binding.set.isEnabled = false
                                    binding.set.text = "未取得"
                                }
                            }else if (badgeType == "5"){
                                binding.title.text = "サイクリング中級者"
                                binding.content.text = "結構慣れましたね！"
                                binding.number.text = "${myBikeDistance}km / 5000km"
                                binding.badgeImage.setImageResource(R.drawable.bike2)
                                if(btn_5==0){
                                    binding.badgeImage.setImageResource(R.drawable.bike2_g)
                                    binding.set.isEnabled = false
                                    binding.set.text = "未取得"
                                }
                            }else if (badgeType == "6"){
                                binding.title.text = "サイクリングマスター"
                                binding.content.text = "サイクリングマスターになりました！"
                                binding.number.text = "${myBikeDistance}km / 10000km"
                                binding.badgeImage.setImageResource(R.drawable.bike3)
                                if(btn_6==0){
                                    binding.badgeImage.setImageResource(R.drawable.bike3_g)
                                    binding.set.isEnabled = false
                                    binding.set.text = "未取得"
                                }
                            }
                        }
                    }



                }else if (badgeType=="7"||badgeType=="8"||badgeType=="9"){
                    CoroutineScope(Dispatchers.Main).launch {
                        val distanceRunResponse = supplementService.totalDistance(token, "R")
                        if(distanceRunResponse.isSuccessful) {
                            var myRunDistance : Double = 0.0

                            when (distanceRunResponse.code()) {
                                200 -> {
                                    myRunDistance = distanceRunResponse.body()!!.distance
                                }
                            }
                            if(badgeType == "7"){
                                binding.title.text = "ランニング初心者"
                                binding.content.text = "どんどん慣れますよ"
                                binding.number.text = "${myRunDistance}km / 100km"
                                binding.badgeImage.setImageResource(R.drawable.run1)
                                if(btn_7==0){
                                    binding.badgeImage.setImageResource(R.drawable.run1_g)
                                    binding.set.isEnabled = false
                                    binding.set.text = "未取得"
                                }
                            }else if (badgeType == "8"){
                                binding.title.text = "ランニング中級者"
                                binding.content.text = "結構慣れましたね！"
                                binding.number.text = "${myRunDistance}km / 500km"
                                binding.badgeImage.setImageResource(R.drawable.run2)
                                if(btn_8==0){
                                    binding.badgeImage.setImageResource(R.drawable.run2_g)
                                    binding.set.isEnabled = false
                                    binding.set.text = "未取得"
                                }
                            }else if (badgeType == "9"){
                                binding.title.text = "ランニングマスター"
                                binding.content.text = "ランニングマスターになりました！"
                                binding.number.text = "${myRunDistance}km / 1000km"
                                binding.badgeImage.setImageResource(R.drawable.run3)
                                if(btn_9==0){
                                    binding.badgeImage.setImageResource(R.drawable.run3_g)
                                    binding.set.isEnabled = false
                                    binding.set.text = "未取得"
                                }
                            }
                        }
                    }


                }else if (badgeType=="10"||badgeType=="11"||badgeType=="12"){
                    supplementService.totalTrackCoutn(token).enqueue(object : Callback<Int> {
                        @SuppressLint("ResourceAsColor")
                        override fun onResponse(call: Call<Int>, response: Response<Int>) {
                            if (response.isSuccessful){
                                var myTrackCount : Int = 0
                                myTrackCount = response.body()!!
                                if(badgeType == "10"){
                                    binding.title.text = "コースメーカ初心者"
                                    binding.content.text = "どんどん慣れますよ"
                                    binding.number.text = "${myTrackCount}回 / 3回"
                                    binding.badgeImage.setImageResource(R.drawable.track1)
                                    if(btn_10==0){
                                        binding.badgeImage.setImageResource(R.drawable.track1_g)
                                        binding.set.isEnabled = false
                                        binding.set.text = "未取得"
                                    }
                                }else if (badgeType == "11"){
                                    binding.title.text = "コースメーカ中級者"
                                    binding.content.text = "結構慣れましたね！"
                                    binding.number.text = "${myTrackCount}回 / 20回"
                                    binding.badgeImage.setImageResource(R.drawable.track2)
                                    if(btn_11==0){
                                        binding.badgeImage.setImageResource(R.drawable.track2_g)
                                        binding.set.isEnabled = false
                                        binding.set.text = "未取得"
                                    }
                                }else if (badgeType == "12"){
                                    binding.title.text = "コースメーカマスター"
                                    binding.content.text = "コースメーカマスターになりました！"
                                    binding.number.text = "${myTrackCount}回 / 50回"
                                    binding.badgeImage.setImageResource(R.drawable.track3)
                                    if(btn_12==0){
                                        binding.badgeImage.setImageResource(R.drawable.track3_g)
                                        binding.set.isEnabled = false
                                        binding.set.text = "未取得"
                                    }
                                }
                            }
                        }
                        override fun onFailure(call: Call<Int>, t: Throwable) {
                        }
                    })

                }
            }

            override fun onFailure(call: Call<LoginUserResponse>, t: Throwable) {
            }
        })








        binding.back.setOnClickListener {
            dismiss()
        }


        binding.set.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val getMyPostsResponse = supplementService.putBadge(token, badge)
            }
            Toast.makeText(context,"バッジ設定ができました",Toast.LENGTH_SHORT).show()
            dismiss()
            }

    }


    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }

}