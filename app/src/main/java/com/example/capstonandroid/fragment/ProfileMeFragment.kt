package com.example.capstonandroid.fragment

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.capstonandroid.R
import com.example.capstonandroid.activity.BadgeActivity
import com.example.capstonandroid.activity.EditProfileActivity
import com.example.capstonandroid.activity.LoginActivity
import com.example.capstonandroid.databinding.FragmentProfileMeBinding
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.dto.*
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private  lateinit var  retrofit: Retrofit  //레트로핏
private  lateinit var supplementService: BackendApi // api

private var chart_max :Float = 0.0f;
private var chart_min :Float = 0.0f;

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
class ProfileMeFragment : Fragment(){
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // 바인딩 객체 타입에 ?를 붙여서 null을 허용 해줘야한다. ( onDestroy 될 때 완벽하게 제거를 하기위해 )
    private var mBinding: FragmentProfileMeBinding? = null
    // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!






//    private val binding: FragmentProfileMeBinding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

    }

    inner class MyXAxisFormatter : ValueFormatter(){
        private val days = arrayOf("6日前","５日前","４日前","３日前","一昨日","昨日","今日")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return days.getOrNull(value.toInt()-1) ?: value.toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentProfileMeBinding.inflate(inflater, container, false)


        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }

    override fun onStart() {



        super.onStart()


        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(activity, EditProfileActivity::class.java)
            startActivity(intent)
        }



//        함수 초기화
        initRetrofit()
////      토큰 불러오기

        val sharedPreference = requireActivity().getSharedPreferences("other", 0)


//      이 타입이 디폴트 값
        var token = "Bearer " + sharedPreference.getString("TOKEN","")
        println("프로필 미 프래그먼트 + "+token)


        var sData = resources.getStringArray(R.array.my_array)


        supplementService.userWeek(token, "R").enqueue(object : Callback<UserWeekResponse>{
            override fun onResponse(
                call: Call<UserWeekResponse>,
                response: Response<UserWeekResponse>
            ) {
                r_today = response.body()!!.today.toFloat()
                r_oneDayAgo = response.body()!!.oneDayAgo.toFloat()
                r_twoDayAgo = response.body()!!.twoDayAgo.toFloat()
                r_threeDayAgo = response.body()!!.threeDayAgo.toFloat()// 여기 디비 수정 해야됨.
                r_fourDayAgo= response.body()!!.fourDayAgo.toFloat()
                r_fiveDayAgo = response.body()!!.fiveDayAgo.toFloat()
                r_sixDayAgo = response.body()!!.sixDayAgo.toFloat()

                println("라이딩"+response.body()!!)
                println(r_sixDayAgo)

                supplementService.userWeek(token,"B").enqueue(object :Callback<UserWeekResponse>{
                    override fun onResponse(
                        call: Call<UserWeekResponse>,
                        response: Response<UserWeekResponse>
                    ) {



                        b_today = response.body()!!.today.toFloat()
                        b_oneDayAgo = response.body()!!.oneDayAgo.toFloat()
                        b_twoDayAgo = response.body()!!.twoDayAgo.toFloat()
                        b_threeDayAgo = response.body()!!.threeDayAgo.toFloat()// 여기 디비 수정 해야됨.
                        b_fourDayAgo= response.body()!!.fourDayAgo.toFloat()
                        b_fiveDayAgo = response.body()!!.fiveDayAgo.toFloat()
                        b_sixDayAgo = response.body()!!.sixDayAgo.toFloat()

                        println(response.body()!!)

//                val entries2 = ArrayList<BarEntry>()
//
//                entries.add(BarEntry(0.9f,r_sixDayAgo))
//                entries.add(BarEntry(1.2f,b_sixDayAgo))
//                entries.add(BarEntry(1.9f,r_fiveDayAgo))
//                entries.add(BarEntry(2.2f,b_fiveDayAgo))
//                entries.add(BarEntry(2.9f,r_fourDayAgo))
//                entries.add(BarEntry(3.2f,b_fourDayAgo))
//                entries.add(BarEntry(3.9f,r_threeDayAgo))
//                entries.add(BarEntry(4.2f,b_threeDayAgo))
//                entries.add(BarEntry(4.9f,r_twoDayAgo))
//                entries.add(BarEntry(5.2f,b_twoDayAgo))
//                entries.add(BarEntry(5.9f,r_oneDayAgo))
//                entries.add(BarEntry(6.2f,b_oneDayAgo))
//                entries.add(BarEntry(6.9f,r_today))
//                entries.add(BarEntry(7.2f,b_today))

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
                        for( i in 0..13){
                            if(max < arr[i]){   // 부호만 바꾸면 최대값이 구해진다.
                                max = arr[i]
                                println(max)
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
                        set2.color =ColorTemplate.rgb("#5db5ef")


                        val data = BarData(set,set2)
                        data.barWidth = 0.2f//막대 너비 설정하기

                        binding.chart.run {
                            this.data = data //차트의 데이터를 data로 설정해줌.
                            setFitBars(true)
                            invalidate()
                        }

                    }

                    override fun onFailure(call: Call<UserWeekResponse>, t: Throwable) {
                    }
                })

            }
            override fun onFailure(call: Call<UserWeekResponse>, t: Throwable) {
            }
        })






        supplementService.userGet(token.toString()).enqueue(object : Callback<LoginUserResponse>{
            override fun onResponse(
                call: Call<LoginUserResponse>,
                response: Response<LoginUserResponse>
            ) {
                println(response.body())
                var loginuserResponse: LoginUserResponse? = response.body()
                var user_name = loginuserResponse?.name
                var user_followers = loginuserResponse!!.followers?.count()
                var user_followings = loginuserResponse!!.followings?.count()
                var user_mmr = loginuserResponse!!.mmr
                var location = loginuserResponse!!.location
                var introduce = loginuserResponse.introduce

                binding.tvName.text = user_name
                binding.tvFollower.text = user_followers.toString()
                binding.tvFollowing.text = user_followings.toString()
                binding.tvMmr.text = user_mmr.toString()
                binding.tvLocation.text = location
                binding.tvIntroduce.text = introduce


                    if (loginuserResponse?.profile.equals(null)||loginuserResponse?.profile.equals("img")){
                    binding.tvProfileMePicture.setImageResource(R.drawable.main_profile)
                }else {

                    val url = response.body()!!.profile
                    Glide.with(this@ProfileMeFragment)
                        .load(url)
                        .circleCrop()
                        .into(binding.tvProfileMePicture)
                }
                if(user_mmr!! >= 0&& user_mmr!! <= 99){
                    binding.medalLayout.setBackgroundResource(R.drawable.medal_bronze)
                }else if (user_mmr >= 100 && user_mmr <= 199){
                    binding.medalLayout.setBackgroundResource(R.drawable.medal_silver)
                }else if (user_mmr >= 200){
                    binding.medalLayout.setBackgroundResource(R.drawable.medal_gold)
                }

            }

            override fun onFailure(call: Call<LoginUserResponse>, t: Throwable) {
            }
        })

        val loginActivity = Intent(requireContext(), LoginActivity::class.java)

        binding.btnBadgeActivity.setOnClickListener{
            val intent = Intent(requireContext(), BadgeActivity::class.java)
            startActivity(intent)
        }
        binding.logout.setOnClickListener {


            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("本当にログアウトしますか。")
                .setPositiveButton("확인") { _, _->

                    supplementService.logOut(token).enqueue(object : Callback<LogoutResponse> {
                        override fun onResponse(
                            call: Call<LogoutResponse>,
                            response: Response<LogoutResponse>
                        ) {
                            if (response.isSuccessful) {
                                println("ログインしました")
                                Toast.makeText(requireContext(),"ログアウトしました", Toast.LENGTH_SHORT).show()

//                  콜백 응답으로 온것
                                println(response.body())
                                startActivity(loginActivity)
                                activity?.finish()

                            } else {
                                println("갔지만 실패")
                                println(response.body())
                                println(response.message())
                                println(response.code())
                            }
                        }

                        override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                            TODO("Not yet implemented")
                        }
                    })



                    var autologin: String = "false"

                    val editor = sharedPreference.edit()
                    editor.putString("autologin", autologin)
                    println("여긴 자동로그인 맞나 아닌가"+ autologin)
                    editor.apply()

                }
                .setNegativeButton("취소") { _, _ ->
                    println("취소 하셨네요")
                }

            builder.show()


        }
    }
    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileMeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
