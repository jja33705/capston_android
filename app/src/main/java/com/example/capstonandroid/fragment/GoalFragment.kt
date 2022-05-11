package com.example.capstonandroid.fragment

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.capstonandroid.GoalDialog
import com.example.capstonandroid.MyApplication
import com.example.capstonandroid.Utils
import com.example.capstonandroid.activity.GoalBikeActivity
import com.example.capstonandroid.activity.GoalRunActivity
import com.example.capstonandroid.activity.SelectTrackActivity
import com.example.capstonandroid.databinding.FragmentGoalBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.UserExerciseRateResponse
import com.example.capstonandroid.network.dto.UserGoalCheckResponse
import com.example.capstonandroid.network.dto.goalDeleteResponse
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private  lateinit var  retrofit: Retrofit  //레트로핏
private  lateinit var supplementService: BackendApi // api

class GoalFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var user_running : Double = 0.0
    var user_riding :Double = 0.0

    var user_goal_running : Double = 0.0
    var user_goal_riding : Double = 0.0

    var user_goal_running_title : String = ""
    var user_goal_running_Goal : Int  = 0
    var user_goal_running_StartDate : String = ""
    var user_goal_running_EndDate : String = ""
    var user_goal_running_ID : Int = 0

    var user_goal_riding_title : String = ""
    var user_goal_riding_Goal : Int  = 0
    var user_goal_riding_StartDate : String = ""
    var user_goal_riding_EndDate : String = ""
    var user_goal_riding_ID : Int = 0

    var totalCalorie : Int = 0
    var totalTime : Int = 0;
    var totalBikeDistance : Int = 0
    var totalRunDistance : Int = 0

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

    override fun onResume() {
        super.onResume()

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
        binding.userExerciseChart.setUsePercentValues(true)
        binding.userGoalRiding.setUsePercentValues(true)
        binding.userGoalRunning.setUsePercentValues(true)


        supplementService.userExerciseRate(token).enqueue(object : Callback<UserExerciseRateResponse>{
            override fun onResponse(
                call: Call<UserExerciseRateResponse>,
                response: Response<UserExerciseRateResponse>
            ) {


                val entries = ArrayList<PieEntry>()
                    user_riding = response.body()!!.B
                    user_running = response.body()!!.R

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
                        centerText = "운동 비율"
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


        CoroutineScope(Dispatchers.Main).launch {
            val distanceBikeResponse = supplementService.totalDistance(token, "B")

            if(distanceBikeResponse.isSuccessful){
                totalBikeDistance = distanceBikeResponse.body()!!.distance

            }

            val distanceRunResponse = supplementService.totalDistance(token, "R")

            if(distanceRunResponse.isSuccessful){
                totalRunDistance = distanceRunResponse.body()!!.distance
            }

            var totalTimeResponse = supplementService.totalTime(token)

            if(totalTimeResponse.isSuccessful){
                totalTime = totalTimeResponse.body()!!
            }

            var totalCalorieResponse = supplementService.totalCalorie(token)

            if(totalCalorieResponse.isSuccessful){
                totalCalorie = totalCalorieResponse.body()!!
            }


            binding.totalDistance.text = (totalBikeDistance+totalRunDistance).toString()+"Km"
            binding.totalTime.text = Utils.timeToStringText(totalTime)
            binding.totalCalorie.text = totalCalorie.toString()+"Cal"
        }


        goal()





        binding.runButton.setOnClickListener {

            MyApplication.prefs.setString("eventType", "R")
            showGoalDialog()

        //            val nextIntent = Intent(requireContext(), GoalRunActivity::class.java)

//            startActivity(nextIntent)
        }

        binding.bikeButton.setOnClickListener {

            MyApplication.prefs.setString("eventType", "B")
            showGoalDialog()
        }

        binding.userGoalRiding.setOnLongClickListener{

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("本当に削除しますか。")
                .setPositiveButton("はい", DialogInterface.OnClickListener{ dialog,id->
                    supplementService.goalDelete(token,user_goal_riding_ID).enqueue(object : Callback<goalDeleteResponse> {
                        override fun onResponse(call: Call<goalDeleteResponse>, response: Response<goalDeleteResponse>) {


                            binding.bikeLayout.visibility = View.GONE
                            binding.bikeLayoutEdit.visibility = View.VISIBLE


                           }
                        override fun onFailure(call: Call<goalDeleteResponse>, t: Throwable) {

                        }
                    })
                })
                .setNegativeButton("いいえ",DialogInterface.OnClickListener{ dialog,id ->
                    println("취소 하셨네요")
                })

            builder.show()
            true
        }
        binding.userGoalRunning.setOnLongClickListener{
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("本当に削除しますか。")
                .setPositiveButton("はい", DialogInterface.OnClickListener{ dialog,id->
                    supplementService.goalDelete(token,user_goal_running_ID).enqueue(object : Callback<goalDeleteResponse> {
                        override fun onResponse(call: Call<goalDeleteResponse>, response: Response<goalDeleteResponse>) {


                            binding.runLayout.visibility = View.GONE
                            binding.runLayoutEdit.visibility = View.VISIBLE
                        }
                        override fun onFailure(call: Call<goalDeleteResponse>, t: Throwable) {

                        }
                    })
                })
                .setNegativeButton("いいえ",DialogInterface.OnClickListener{ dialog,id ->
                    println("취소 하셨네요")
                })

            builder.show()
            true
        }

        binding.message3.setOnLongClickListener {
            val intent = Intent(requireContext(), SelectTrackActivity::class.java)
            startActivity(intent)
            true
        }
    }


    private fun showGoalDialog() {
        GoalDialog(requireContext()) {
            if(it == "Hello"){
            goal()
            }
        }.show()
    }


    private fun goal(){

        val sharedPreference = requireActivity().getSharedPreferences("other", 0)

        var token = "Bearer " + sharedPreference.getString("TOKEN","")

        supplementService.userGoalCheck(token).enqueue(object : Callback<UserGoalCheckResponse>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<UserGoalCheckResponse>,
                response: Response<UserGoalCheckResponse>
            ) {
                val entries = ArrayList<PieEntry>()
                val entries2 = ArrayList<PieEntry>()
                val entries3 = ArrayList<PieEntry>()

                if(response.body()!!.run.size==0){
                    binding.runLayout.visibility = View.GONE
                    binding.runLayoutEdit.visibility = View.VISIBLE
                }
                else {

                    binding.runLayout.visibility = View.VISIBLE
                    binding.runLayoutEdit.visibility = View.GONE

                    user_goal_running = response.body()!!.run[0]?.progress.toDouble()
                    user_goal_running_title = response.body()!!.run[0]?.title
                    user_goal_running_Goal = response.body()!!.run[0]?.goalDistance
                    user_goal_running_StartDate = response.body()!!.run[0]!!.firstDate
                    user_goal_running_EndDate = response.body()!!.run[0]!!.lastDate
                    user_goal_running_ID = response.body()!!.run[0].id
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
                binding.RunningTitle.setText(user_goal_running_title)
                binding.RunningGoal.setText("목표 거리 : "+user_goal_running_Goal.toString()+"km")
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
                    valueTextSize = 2f

                }

                val pieData2 = PieData(pieDataSet2)
                binding.userGoalRunning.apply {
                    data = pieData2
                    description.isEnabled = false
                    isRotationEnabled = false
                    centerText = "나의 달리기 목표"
                    setCenterTextSize(8f)
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

                    user_goal_riding_Goal = response.body()!!.bike[0]?.goalDistance
                    user_goal_riding_StartDate = response.body()!!.bike[0]!!.firstDate
                    user_goal_riding_EndDate = response.body()!!.bike[0]!!.lastDate
                    user_goal_riding_ID = response.body()!!.bike[0].id

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
                    valueTextSize = 2f

                }
                val pieData3 = PieData(pieDataSet3)
                binding.userGoalRiding.apply {
                    data = pieData3
                    description.isEnabled = false
                    isRotationEnabled = false
                    centerText = "나의 달리기 목표"
                    setCenterTextSize(8f)
                    setEntryLabelColor(Color.BLACK)

                    setTouchEnabled(false) // 그래프 터치해도 아무 변화없게 막음
                    animateY(1000, Easing.EaseInOutQuad)
                    animate()
                }
                binding.RidingTitle.setText(user_goal_riding_title)
                binding.RidingGoal.setText("목표 거리 : "+user_goal_riding_Goal.toString()+"km")
                binding.RidingStartDate.setText("시작 : "+user_goal_riding_StartDate)
                binding.RidingEndDate.setText("종료 : "+user_goal_riding_EndDate)
            }

            override fun onFailure(call: Call<UserGoalCheckResponse>, t: Throwable) {
            }
        })

<<<<<<< HEAD
        binding.runButton.setOnClickListener {

            val nextIntent = Intent(requireContext(), GoalRunActivity::class.java)

            startActivity(nextIntent)
        }
        binding.bikeButton.setOnClickListener {

            val nextIntent = Intent(requireContext(), GoalBikeActivity::class.java)

            startActivity(nextIntent)
        }

        binding.userGoalRiding.setOnLongClickListener{

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("本当に削除しますか。")
                .setPositiveButton("はい", DialogInterface.OnClickListener{ dialog,id->
                    supplementService.goalDelete(token,user_goal_riding_ID).enqueue(object : Callback<goalDeleteResponse> {
                        override fun onResponse(call: Call<goalDeleteResponse>, response: Response<goalDeleteResponse>) {


                            binding.bikeLayout.visibility = View.GONE
                            binding.bikeLayoutEdit.visibility = View.VISIBLE


                           }
                        override fun onFailure(call: Call<goalDeleteResponse>, t: Throwable) {

                        }
                    })
                })
                .setNegativeButton("いいえ",DialogInterface.OnClickListener{ dialog,id ->
                    println("취소 하셨네요")
                })

            builder.show()
            true
        }
        binding.userGoalRunning.setOnLongClickListener{
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("本当に削除しますか。")
                .setPositiveButton("はい", DialogInterface.OnClickListener{ dialog,id->
                    supplementService.goalDelete(token,user_goal_running_ID).enqueue(object : Callback<goalDeleteResponse> {
                        override fun onResponse(call: Call<goalDeleteResponse>, response: Response<goalDeleteResponse>) {


                            binding.runLayout.visibility = View.GONE
                            binding.runLayoutEdit.visibility = View.VISIBLE
                        }
                        override fun onFailure(call: Call<goalDeleteResponse>, t: Throwable) {

                        }
                    })
                })
                .setNegativeButton("いいえ",DialogInterface.OnClickListener{ dialog,id ->
                    println("취소 하셨네요")
                })

            builder.show()
            true
        }

        binding.message3.setOnLongClickListener {
            val intent = Intent(requireContext(), SelectTrackActivity::class.java)
            startActivity(intent)
            true
        }
    }
    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GoalFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
=======
>>>>>>> 2e92e5a (5/10)
    }

    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }
}


