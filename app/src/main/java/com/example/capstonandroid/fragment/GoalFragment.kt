package com.example.capstonandroid.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.FragmentGoalBinding
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.dto.UserWeekResponse
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
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
        mBinding = FragmentGoalBinding.inflate(inflater, container, false)


        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        var today :Float = 0f;
        var oneDayAgo :Float = 0f;
        var twoDayAgo :Float = 0f;
        var threeDayAgo :Float = 0f;
        var fourDayAgo :Float = 0f;
        var fiveDayAgo :Float = 0f;
        var sixDayAgo :Float = 0f;
        initRetrofit()

        val sharedPreference = requireActivity().getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var token = "Bearer " + sharedPreference.getString("TOKEN","")
        println("홈 프레그먼트"+token)


        var sData = resources.getStringArray(R.array.my_array)
        var adapter =
            activity?.let { ArrayAdapter<String>(it,android.R.layout.simple_spinner_item,sData) }
        binding.spProfileMeSpinner.adapter = adapter

        binding.spProfileMeSpinner.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent : AdapterView<*>?, view: View?, position1: Int, Int: Long) {

                val event = "R"

                supplementService.userWeek(token, event).enqueue(object : Callback<UserWeekResponse>{
                    override fun onResponse(
                        call: Call<UserWeekResponse>,
                        response: Response<UserWeekResponse>
                    ) {
                        today = response.body()!!.today.toFloat()
                        oneDayAgo = response.body()!!.oneDayAgo.toFloat()
                        twoDayAgo = response.body()!!.twoDayAgo.toFloat()
                        threeDayAgo = response.body()!!.threeDayAgo.toFloat()// 여기 디비 수정 해야됨.
                        fourDayAgo= response.body()!!.fourDayAgo.toFloat()
                        fiveDayAgo = response.body()!!.fiveDayAgo.toFloat()
                        sixDayAgo = response.body()!!.sixDayAgo.toFloat()

                        println(response.body()!!)
                    }

                    override fun onFailure(call: Call<UserWeekResponse>, t: Throwable) {
                    }
                })


                if (position1 == 0) { val entries = ArrayList<BarEntry>()
                    entries.add(BarEntry(1.2f,sixDayAgo))
                    entries.add(BarEntry(2.2f,fiveDayAgo))
                    entries.add(BarEntry(3.2f,fourDayAgo))
                    entries.add(BarEntry(4.2f,threeDayAgo))
                    entries.add(BarEntry(5.2f,twoDayAgo))
                    entries.add(BarEntry(6.2f,oneDayAgo))
                    entries.add(BarEntry(7.2f,today))


                    binding.chart.run {
                        description.isEnabled = false //차트 옆에 별도로 표기되는 description이다. false로 설정하여 안보이게 했다.
                        setMaxVisibleValueCount(7) // 최대 보이는 그래프 개수를 7개로 정해주었다.
                        setPinchZoom(false) // 핀치줌(두손가락으로 줌인 줌 아웃하는것) 설정
                        setDrawBarShadow(false)//그래프의 그림자
                        setDrawGridBackground(false)//격자구조 넣을건지
                        axisLeft.run { //왼쪽 축. 즉 Y방향 축을 뜻한다.
                            axisMaximum = 101f //100 위치에 선을 그리기 위해 101f로 맥시멈을 정해주었다
                            axisMinimum = 0f // 최소값 0
                            granularity = 50f // 50 단위마다 선을 그리려고 granularity 설정 해 주었다.
                            //위 설정이 20f였다면 총 5개의 선이 그려졌을 것
                            setDrawLabels(true) // 값 적는거 허용 (0, 50, 100)
                            setDrawGridLines(true) //격자 라인 활용
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
                        axisRight.isEnabled = false // 오른쪽 Y축을 안보이게 해줌.
                        setTouchEnabled(false) // 그래프 터치해도 아무 변화없게 막음
                        animateY(1000) // 밑에서부터 올라오는 애니매이션 적용
                        legend.isEnabled = false //차트 범례 설정

                    }

                    var set = BarDataSet(entries,"DataSet")//데이터셋 초기화 하기
                    set.color = ContextCompat.getColor(requireContext()!!,R.color.red)

                    val dataSet :ArrayList<IBarDataSet> = ArrayList()
                    dataSet.add(set)
                    val data = BarData(dataSet)
                    data.barWidth = 0.3f//막대 너비 설정하기
                    binding.chart.run {
                        this.data = data //차트의 데이터를 data로 설정해줌.
                        setFitBars(true)
                        invalidate()
                    }
                } else {


                    val event = "B"

                    supplementService.userWeek(token, event).enqueue(object : Callback<UserWeekResponse>{
                        override fun onResponse(
                            call: Call<UserWeekResponse>,
                            response: Response<UserWeekResponse>
                        ) {
                            today = response.body()!!.today.toFloat()
                            oneDayAgo = response.body()!!.oneDayAgo.toFloat()
                            twoDayAgo = response.body()!!.twoDayAgo.toFloat()
                            threeDayAgo = response.body()!!.threeDayAgo.toFloat()// 여기 디비 수정 해야됨.
                            fourDayAgo= response.body()!!.fourDayAgo.toFloat()
                            fiveDayAgo = response.body()!!.fiveDayAgo.toFloat()
                            sixDayAgo = response.body()!!.sixDayAgo.toFloat()

                            println(response.body()!!)
                        }

                        override fun onFailure(call: Call<UserWeekResponse>, t: Throwable) {
                        }
                    })

                    val entries = ArrayList<BarEntry>()
                    entries.add(BarEntry(1.2f,sixDayAgo))
                    entries.add(BarEntry(2.2f,fiveDayAgo))
                    entries.add(BarEntry(3.2f,fourDayAgo))
                    entries.add(BarEntry(4.2f,threeDayAgo))
                    entries.add(BarEntry(5.2f,twoDayAgo))
                    entries.add(BarEntry(6.2f,oneDayAgo))
                    entries.add(BarEntry(7.2f,today))

                    binding.chart.run {
                        description.isEnabled = false //차트 옆에 별도로 표기되는 description이다. false로 설정하여 안보이게 했다.
                        setMaxVisibleValueCount(7) // 최대 보이는 그래프 개수를 7개로 정해주었다.
                        setPinchZoom(false) // 핀치줌(두손가락으로 줌인 줌 아웃하는것) 설정
                        setDrawBarShadow(false)//그래프의 그림자
                        setDrawGridBackground(false)//격자구조 넣을건지
                        axisLeft.run { //왼쪽 축. 즉 Y방향 축을 뜻한다.
                            axisMaximum = 101f //100 위치에 선을 그리기 위해 101f로 맥시멈을 정해주었다
                            axisMinimum = 0f // 최소값 0
                            granularity = 50f // 50 단위마다 선을 그리려고 granularity 설정 해 주었다.
                            //위 설정이 20f였다면 총 5개의 선이 그려졌을 것
                            setDrawLabels(true) // 값 적는거 허용 (0, 50, 100)
                            setDrawGridLines(true) //격자 라인 활용
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
                        axisRight.isEnabled = false // 오른쪽 Y축을 안보이게 해줌.
                        setTouchEnabled(false) // 그래프 터치해도 아무 변화없게 막음
                        animateY(1000) // 밑에서부터 올라오는 애니매이션 적용
                        legend.isEnabled = false //차트 범례 설정

                    }

                    var set = BarDataSet(entries,"DataSet")//데이터셋 초기화 하기
                    set.color = ContextCompat.getColor(requireContext()!!,R.color.red)

                    val dataSet :ArrayList<IBarDataSet> = ArrayList()
                    dataSet.add(set)
                    val data = BarData(dataSet)
                    data.barWidth = 0.3f//막대 너비 설정하기
                    binding.chart.run {
                        this.data = data //차트의 데이터를 data로 설정해줌.
                        setFitBars(true)
                        invalidate()
                    }

                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
        binding.spProfileMeSpinner.setSelection(1)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)  {
        super.onViewCreated(view, savedInstanceState)


    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PersonalMeFragment.
         */
        // TODO: Rename and change types and number of parameters
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