package com.example.capstonandroid.fragment

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.capstonandroid.R
import com.example.capstonandroid.activity.GoalBikeActivity
import com.example.capstonandroid.activity.GoalRunActivity
import com.example.capstonandroid.activity.MainActivity
import com.example.capstonandroid.databinding.FragmentGoalBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.UserExerciseRateResponse
import com.example.capstonandroid.network.dto.UserGoalCheckResponse
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
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

        var user_running : Double = 0.0
        var user_riding :Double = 0.0

        var user_goal_running : Double = 0.0
        var user_goal_riding : Double = 0.0

        var user_goal_running_title : String = ""
        var user_goal_running_StartDate : String = ""
        var user_goal_running_EndDate : String = ""

        var user_goal_riding_title : String = ""
        var user_goal_riding_StartDate : String = ""
        var user_goal_riding_EndDate : String = ""

        binding.userExerciseChart.setUsePercentValues(true)
        binding.userGoalRiding.setUsePercentValues(true)
        binding.userGoalRunning.setUsePercentValues(true)

        val entries = ArrayList<PieEntry>()
        val entries2 = ArrayList<PieEntry>()
        val entries3 = ArrayList<PieEntry>()

        supplementService.userExerciseRate(token).enqueue(object : Callback<UserExerciseRateResponse>{
            override fun onResponse(
                call: Call<UserExerciseRateResponse>,
                response: Response<UserExerciseRateResponse>
            ) {
//                    user_riding = response.body()!!.B
//                    user_running = response.body()!!.R

                if(user_riding==0.0&&user_running==0.0){
                    println("정보가 없어요~")
                    binding.userExerciseChart.visibility = View.GONE
                    binding.message3.visibility = View.VISIBLE

                }
//              라이딩 정보가 없을떄?
                else if(user_riding==0.0){
                    entries.add(PieEntry(user_running.toFloat(),"달리기"))

                    val colorsItems = ArrayList<Int>()
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

//              러닝 정보가 없을때..
                }else if(user_running==0.0){
                    entries.add(PieEntry(user_riding.toFloat(),"자전거"))

                    val colorsItems = ArrayList<Int>()
                    colorsItems.add(ColorTemplate.rgb("#5db5ef"))
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
                }
//              다 있을때 !
                else {

                    entries.add(PieEntry(user_riding.toFloat(), "자전거"))
                    entries.add(PieEntry(user_running.toFloat(), "달리기"))
//
//                    entries.add(PieEntry(80f, "자전거"))
//                    entries.add(PieEntry(20f, "달리기"))
                    val colorsItems = ArrayList<Int>()
                    colorsItems.add(ColorTemplate.rgb("#5db5ef"))
                    colorsItems.add(ColorTemplate.rgb("#6fcdcd"))
                    colorsItems.add(ColorTemplate.getHoloBlue())

                    val pieDataSet = PieDataSet(entries, "")
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
                        animateY(1000, Easing.EaseInOutQuad)
                        animate()
                    }
                }
            }
            override fun onFailure(call: Call<UserExerciseRateResponse>, t: Throwable) {
//                TODO("Not yet implemented")
            }
        })










        supplementService.userGoalCheck(token).enqueue(object : Callback<UserGoalCheckResponse>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<UserGoalCheckResponse>,
                response: Response<UserGoalCheckResponse>
            ) {


                if(response.body()!!.run.size==0){
                    binding.runLayout.visibility = View.GONE
                    binding.runLayoutEdit.visibility = View.VISIBLE
                }
                else {

                    binding.runLayout.visibility = View.VISIBLE
                    binding.runLayoutEdit.visibility = View.GONE

                    user_goal_running = response.body()!!.run[0]?.progress.toDouble()
                    user_goal_running_title = response.body()!!.run[0]?.title

                    user_goal_running_StartDate = response.body()!!.run[0]!!.firstDate
                    user_goal_running_EndDate = response.body()!!.run[0]!!.lastDate
                }

//                val dateParse = LocalDate.parse(user_goal_running_StartDate)
//                val year : Int = dateParse.get(ChronoField.YEAR)
//                println(year)
//                val month : Int = dateParse.get(ChronoField.MONTH_OF_YEAR)
//                println(month)
//                val day : Int = dateParse.get(ChronoField.DAY_OF_MONTH)
//                println(day)
//
//                var date = LocalDate.parse(user_goal_running_StartDate)
//                var formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.KOREAN)
//
//                println(date.format(formatter).toString())
                binding.RunningGoal.setText(user_goal_running_title)
                binding.RunningStartDate.setText("시작 : "+user_goal_running_StartDate)
                binding.RunningEndDate.setText("종료 : "+user_goal_running_EndDate)


                entries2.add(PieEntry(user_goal_running.toFloat(),"달리기"))
                entries2.add(PieEntry(100-user_goal_running.toFloat(),"남은 목표"))
                val colorsItems2 = ArrayList<Int>()
                colorsItems2.add(ColorTemplate.rgb("#6fcdcd"))
                colorsItems2.add(ColorTemplate.rgb("#dcdcdc"))
                colorsItems2.add(ColorTemplate.getHoloBlue())

                val pieDataSet2 = PieDataSet(entries2,"")
                pieDataSet2.apply {
                    colors = colorsItems2
                    valueTextColor = Color.BLACK
                    valueTextSize = 14f

                }

                val pieData2 = PieData(pieDataSet2)
                binding.userGoalRunning.apply {
                    data = pieData2
                    description.isEnabled = false
                    isRotationEnabled = false
                    centerText = "나의 달리기 목표"
                    setCenterTextSize(10f)
                    setEntryLabelColor(Color.BLACK)
                    maxAngle
                    setTouchEnabled(false) // 그래프 터치해도 아무 변화없게 막음
                    animateY(1000, Easing.EaseInOutQuad)
                    animate()
                }


                if(response.body()!!.bike.size==0){
                    binding.bikeLayout.visibility = View.GONE
                    binding.bikeLayoutEdit.visibility = View.VISIBLE
                }


                else {
                    binding.bikeLayout.visibility = View.VISIBLE
                    binding.bikeLayoutEdit.visibility = View.GONE

                user_goal_riding = response.body()!!.bike[0]!!.progress.toDouble()
                user_goal_riding_title = response.body()!!.bike[0]!!.title
                user_goal_riding_StartDate = response.body()!!.bike[0]!!.firstDate
                user_goal_riding_EndDate = response.body()!!.bike[0]!!.lastDate

                }
                entries3.add(PieEntry(user_goal_riding.toFloat(),"자전거"))
                entries3.add(PieEntry(100-user_goal_riding.toFloat(),"남은 목표"))
                val colorsItems3 = ArrayList<Int>()
                colorsItems3.add(ColorTemplate.rgb("#5db5ef"))
                colorsItems3.add(ColorTemplate.rgb("#dcdcdc"))
                colorsItems3.add(ColorTemplate.getHoloBlue())

                val pieDataSet3 = PieDataSet(entries3,"")
                pieDataSet3.apply {
                    colors = colorsItems3
                    valueTextColor = Color.BLACK
                    valueTextSize = 14f

                }
                val pieData3 = PieData(pieDataSet3)
                binding.userGoalRiding.apply {
                    data = pieData3
                    description.isEnabled = false
                    isRotationEnabled = false
                    centerText = "나의 달리기 목표"
                    setCenterTextSize(10f)
                    setEntryLabelColor(Color.BLACK)

                    setTouchEnabled(false) // 그래프 터치해도 아무 변화없게 막음
                    animateY(1000, Easing.EaseInOutQuad)
                    animate()
                }


                binding.RidingGoal.setText(user_goal_riding_title)
                binding.RidingStartDate.setText("시작 : "+user_goal_riding_StartDate)
                binding.RidingEndDate.setText("종료 : "+user_goal_riding_EndDate)
            }

            override fun onFailure(call: Call<UserGoalCheckResponse>, t: Throwable) {
            }
        })

        binding.runButton.setOnClickListener {

            val nextIntent = Intent(requireContext(), GoalRunActivity::class.java)

            startActivity(nextIntent)
        }
        binding.bikeButton.setOnClickListener {

            val nextIntent = Intent(requireContext(), GoalBikeActivity::class.java)

            startActivity(nextIntent)
        }
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