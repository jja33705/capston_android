package com.example.capstonandroid.fragment

import android.R
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.size
import androidx.fragment.app.Fragment
import com.example.capstonandroid.databinding.FragmentGoalBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.UserExerciseRateResponse
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.ColorTemplate.COLORFUL_COLORS
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private  lateinit var  retrofit: Retrofit  //레트로핏
private  lateinit var supplementService: BackendApi // api

class PersonalMeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null



    // 바인딩 객체 타입에 ?를 붙여서 null을 허용 해줘야한다. ( onDestroy 될 때 완벽하게 제거를 하기위해 )
    private var mBinding: FragmentGoalBinding? = null
    // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        mBinding = FragmentGoalBinding.inflate(inflater, container, false)

        initRetrofit()

        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)  {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onStart() {
        super.onStart()

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

////      토큰 불러오기
        val sharedPreference = requireActivity().getSharedPreferences("other", 0)
        var token = "Bearer " + sharedPreference.getString("TOKEN","")

        var user_riding :Double = 0.0
        var user_running : Double = 0.0

        binding.userExerciseChart.setUsePercentValues(true)
        binding.userGoalRidding.setUsePercentValues(true)
        binding.userGoalRunning.setUsePercentValues(true)
        val entries = ArrayList<PieEntry>()
        supplementService.userExerciseRate(token).enqueue(object : Callback<UserExerciseRateResponse>{
            override fun onResponse(
                call: Call<UserExerciseRateResponse>,
                response: Response<UserExerciseRateResponse>
            ) {
                    user_riding = response.body()!!.B
                    user_running = response.body()!!.R


                if(user_riding==0.0&&user_running==0.0)
                {
                }else if(user_riding==0.0){
                    entries.add(PieEntry(user_running.toFloat(),"달리기"))
                }else if(user_running==0.0){
                    entries.add(PieEntry(user_riding.toFloat(),"자전거"))
                }

                val colorsItems = ArrayList<Int>()
                colorsItems.add(ColorTemplate.rgb("#5db5ef"))
                colorsItems.add(ColorTemplate.rgb("#6fcdcd"))
                colorsItems.add(ColorTemplate.getHoloBlue())

                val pieDataSet = PieDataSet(entries,"")
                pieDataSet.apply {
                    colors = colorsItems
                    valueTextColor = Color.BLACK
                    valueTextSize = 14f

                }
                val pieData = PieData(pieDataSet)
                binding.userExerciseChart.apply {
                    data = pieData
                    description.isEnabled = false
                    isRotationEnabled = false
                    centerText = "나의 운동 비율"
                    setCenterTextSize(14f)
                    setEntryLabelColor(Color.BLACK)

                    setTouchEnabled(false) // 그래프 터치해도 아무 변화없게 막음
                    animateY(1000,Easing.EaseInOutQuad)
                    animate()
                }

                binding.userGoalRunning.apply {
                    data = pieData
                    description.isEnabled = false
                    isRotationEnabled = false
                    centerText = "나의 운동 비율"
                    setCenterTextSize(14f)
                    setEntryLabelColor(Color.BLACK)

                    setTouchEnabled(false) // 그래프 터치해도 아무 변화없게 막음
                    animateY(1000,Easing.EaseInOutQuad)
                    animate()
                }

                binding.userGoalRidding.apply {
                    data = pieData
                    description.isEnabled = false
                    isRotationEnabled = false
                    centerText = "나의 운동 비율"
                    setCenterTextSize(14f)
                    setEntryLabelColor(Color.BLACK)

                    setTouchEnabled(false) // 그래프 터치해도 아무 변화없게 막음
                    animateY(1000,Easing.EaseInOutQuad)
                    animate()
                }
            }

            override fun onFailure(call: Call<UserExerciseRateResponse>, t: Throwable) {
//                TODO("Not yet implemented")
            }
        })





    }
    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PersonalMeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }

    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }
}