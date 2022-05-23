package com.example.capstonandroid.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityProfileBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.Profile
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class ProfileActivity : AppCompatActivity() {
    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var retrofit: Retrofit  //레트로핏
    private lateinit var supplementService: BackendApi // api

    private var chart_max :Float = 0.0f;
    private var chart_min :Float = 0.0f;

    private lateinit var profile: Profile
    private var followCheck = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "プロフィール"

        initRetrofit()

        var r_today :Float = 0f;
        var r_oneDayAgo :Float = 0f;
        var r_twoDayAgo :Float = 0f;
        var r_threeDayAgo :Float = 0f;
        var r_fourDayAgo :Float = 0f;
        var r_fiveDayAgo :Float = 0f;
        var r_sixDayAgo :Float = 0f;

        var b_today :Float = 0f;
        var b_oneDayAgo :Float = 0f;
        var b_twoDayAgo :Float = 0f;
        var b_threeDayAgo :Float = 0f;
        var b_fourDayAgo :Float = 0f;
        var b_fiveDayAgo :Float = 0f;
        var b_sixDayAgo :Float = 0f;

        CoroutineScope(Dispatchers.Main).launch {
            var token = "Bearer ${getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")}"

            val getUserResponse = supplementService.getUser(token)
            if (getUserResponse.isSuccessful) {
                val myId = getUserResponse.body()!!.id
                var userId = intent.getIntExtra("userId", -1)

                val getProfileResponse = supplementService.getProfile(token, myId, userId)
                if (getProfileResponse.isSuccessful) {
                    profile = getProfileResponse.body()!!
                    followCheck = profile.followCheck

                    binding.tvName.text = profile.name
                    binding.tvFollower.text = profile.followers.count().toString()
                    binding.tvFollowing.text = profile.followings.count().toString()
                    binding.tvMmr.text = profile.mmr.toString()
                    binding.tvLocation.text = profile.location
                    binding.tvIntroduce.text = profile.introduce

                    when (followCheck) {
                        // 팔로우 되어있는 상태
                        1 -> {
                            binding.btnFollow.setBackgroundResource(R.drawable.btn_follow_request_delete)
                            binding.btnFollow.text = "フォロー中"
                            binding.btnFollow.setTextColor(Color.BLACK)
                        }

                        // 팔로우 되어있지 않는 상태
                        2 -> {

                        }

                        // 팔로우가 요청되어 있는 상태
                        3 -> {
                            changeFollowButtonToFollowRequested()
                        }
                    }

                    b_today = getProfileResponse.body()!!.bikeWeekData.today.toFloat()
                    b_oneDayAgo  = getProfileResponse.body()!!.bikeWeekData.oneDayAgo.toFloat()
                    b_twoDayAgo  = getProfileResponse.body()!!.bikeWeekData.twoDayAgo.toFloat()
                    b_threeDayAgo  = getProfileResponse.body()!!.bikeWeekData.threeDayAgo.toFloat()
                    b_fourDayAgo  = getProfileResponse.body()!!.bikeWeekData.fourDayAgo.toFloat()
                    b_fiveDayAgo = getProfileResponse.body()!!.bikeWeekData.fiveDayAgo.toFloat()
                    b_sixDayAgo  = getProfileResponse.body()!!.bikeWeekData.sixDayAgo.toFloat()

                    r_today = getProfileResponse.body()!!.runWeekData.today.toFloat()
                    r_oneDayAgo = getProfileResponse.body()!!.runWeekData.oneDayAgo.toFloat()
                    r_twoDayAgo = getProfileResponse.body()!!.runWeekData.twoDayAgo.toFloat()
                    r_threeDayAgo = getProfileResponse.body()!!.runWeekData.threeDayAgo.toFloat()
                    r_fourDayAgo = getProfileResponse.body()!!.runWeekData.fourDayAgo.toFloat()
                    r_fiveDayAgo = getProfileResponse.body()!!.runWeekData.fiveDayAgo.toFloat()
                    r_sixDayAgo = getProfileResponse.body()!!.runWeekData.sixDayAgo.toFloat()


                    val entries = ArrayList<BarEntry>()

                    entries.add(BarEntry(0.9f,r_sixDayAgo))
                    entries.add(BarEntry(1.9f,r_fiveDayAgo))
                    entries.add(BarEntry(2.9f,r_fourDayAgo))
                    entries.add(BarEntry(3.9f,r_threeDayAgo))
                    entries.add(BarEntry(4.9f,r_twoDayAgo))
                    entries.add(BarEntry(5.9f,r_oneDayAgo))
                    entries.add(BarEntry(6.9f,r_today))


                    val entries2 = ArrayList<BarEntry>()

                    entries2.add(BarEntry(1.2f,b_sixDayAgo))
                    entries2.add(BarEntry(2.2f,b_fiveDayAgo))
                    entries2.add(BarEntry(3.2f,b_fourDayAgo))
                    entries2.add(BarEntry(4.2f,b_threeDayAgo))
                    entries2.add(BarEntry(5.2f,b_twoDayAgo))
                    entries2.add(BarEntry(6.2f,b_oneDayAgo))
                    entries2.add(BarEntry(7.2f,b_today))

                    val arr: Array<Float> = arrayOf(r_sixDayAgo, b_sixDayAgo, r_fiveDayAgo,b_fiveDayAgo,r_fourDayAgo,b_fourDayAgo,r_threeDayAgo,b_threeDayAgo,
                        r_twoDayAgo,b_twoDayAgo,r_oneDayAgo,b_oneDayAgo,r_today,b_today)


                    var max = arr[0]
                    for( i in 0 until arr.lastIndex){
                        if(max < arr[i]){   // 부호만 바꾸면 최대값이 구해진다.
                            max = arr[i]
                        }

                        if (max<5)
                        {
                            chart_max = 5f
                        }else {
                            chart_max = max
                        }}

                    binding.chart.run {
                        description.isEnabled = false //차트 옆에 별도로 표기되는 description이다. false로 설정하여 안보이게 했다.
                        setMaxVisibleValueCount(14) // 최대 보이는 그래프 개수를 7개로 정해주었다.
                        setPinchZoom(false) // 핀치줌(두손가락으로 줌인 줌 아웃하는것) 설정
                        setDrawBarShadow(false)//그래프의 그림자
                        setDrawGridBackground(false)//격자구조 넣을건지
                        axisLeft.run { //왼쪽 축. 즉 Y방향 축을 뜻한다.
                            axisMaximum = chart_max //100 위치에 선을 그리기 위해 101f로 맥시멈을 정해주었다
                            axisMinimum = 0f // 최소값 0
                            granularity = chart_max/5 // 50 단위마다 선을 그리려고 granularity 설정 해 주었다.
                            //위 설정이 20f였다면 총 5개의 선이 그려졌을 것
                            setDrawLabels(true) // 값 적는거 허용 (0, 50, 100)
                            setDrawGridLines(false) //격자 라인 활용
                            setDrawAxisLine(false) // 축 그리기 설정
                            axisLineColor = ContextCompat.getColor(context,R.color.black) // 축 색깔 설정
                            gridColor = ContextCompat.getColor(context,R.color.blue) // 축 아닌 격자 색깔 설정
                            textColor = ContextCompat.getColor(context,R.color.blue) // 라벨 텍스트 컬러 설정
                            textSize = 14f //라벨 텍스트 크기

                        }
                        xAxis.run {
                            position = XAxis.XAxisPosition.BOTTOM//X축을 아래에다가 둔다.
                            granularity = 1f // 1 단위만큼 간격 두기
                            setDrawAxisLine(true) // 축 그림
                            setDrawGridLines(false) // 격자
                            textColor = ContextCompat.getColor(context,R.color.black) //라벨 색상
                            valueFormatter = MyXAxisFormatter() // 축 라벨 값 바꿔주기 위함
                            textSize = 12f // 텍스트 크기
                        }
                        axisRight.isEnabled =false  // 오른쪽 Y축을 안보이게 해줌.
                        setTouchEnabled(false) // 그래프 터치해도 아무 변화없게 막음
                        animateY(1000) // 밑에서부터 올라오는 애니매이션 적용
                        legend.isEnabled = false //차트 범례 설정

                    }


                    var set = BarDataSet(entries,"DataSet")//데이터셋 초기화 하기
                    var set2 = BarDataSet(entries2,"DataSet")// 데이터 셋 초기화하게

                    set.color = ColorTemplate.rgb("#6fcdcd")
                    set2.color = ColorTemplate.rgb("#5db5ef")


                    val data = BarData(set,set2)
                    data.barWidth = 0.2f//막대 너비 설정하기

                    binding.chart.run {
                        this.data = data //차트의 데이터를 data로 설정해줌.
                        setFitBars(true)
                        invalidate()
                    }


                    if (getProfileResponse!!.body()!!.equals(null)||getProfileResponse!!.body()!!.profile.equals("img")){
                        binding.tvProfileMePicture.setImageResource(R.drawable.main_profile)
                    }else {

                        val url = getProfileResponse!!.body()!!.profile
                        Glide.with(this@ProfileActivity)
                            .load(url)
                            .circleCrop()
                            .into(binding.tvProfileMePicture)
                    }
                    if(profile.mmr >= 0 && profile.mmr <= 99){
                        binding.medalLayout.setBackgroundResource(R.drawable.medal_bronze)
                    }else if (profile.mmr >= 100 && profile.mmr <= 199){
                        binding.medalLayout.setBackgroundResource(R.drawable.medal_silver)
                    }else if (profile.mmr >= 200){
                        binding.medalLayout.setBackgroundResource(R.drawable.medal_gold)
                    }
                }
            }
        }

        binding.btnFollow.setOnClickListener {
            val token = "Bearer ${getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")}"
            when (followCheck) {
                // 팔로우 되어있는 상태
                1 -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        val unFollowResponse = supplementService.unFollow(token, profile.id)
                        if (unFollowResponse.isSuccessful) {
                            followCheck = 2
                            changeFollowButtonToFollow()
                        }
                    }
                }

                // 팔로우 되어있지 않는 상태
                2 -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        val followRequestResponse = supplementService.followRequest(token, profile.id)
                        if (followRequestResponse.isSuccessful) {
                            followCheck = 3
                            changeFollowButtonToFollowRequested()
                        }
                    }
                }

                // 팔로우가 요청되어 있는 상태
                3 -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        val cancelFollowRequestResponse = supplementService.cancelFollowRequest(token, profile.id)
                        if (cancelFollowRequestResponse.isSuccessful) {
                            followCheck = 2
                            changeFollowButtonToFollow()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    inner class MyXAxisFormatter : ValueFormatter(){
        private val days = arrayOf("6日前","５日前","４日前","３日前","一昨日","昨日","今日")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return days.getOrNull(value.toInt()-1) ?: value.toString()
        }
    }

    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }

    // 팔로우 요청 보낸 상태로 바꿈
    private fun changeFollowButtonToFollowRequested() {
        binding.btnFollow.setBackgroundResource(R.drawable.btn_follow_request_delete)
        binding.btnFollow.text = "リクエスト中"
        binding.btnFollow.setTextColor(Color.BLACK)
    }

    // 팔로우 요청 보내기 버튼으로 바꿈
    private fun changeFollowButtonToFollow() {
        binding.btnFollow.setBackgroundResource(R.drawable.btn_follow_request_accept)
        binding.btnFollow.text = "フォロー"
        binding.btnFollow.setTextColor(Color.WHITE)
    }
}