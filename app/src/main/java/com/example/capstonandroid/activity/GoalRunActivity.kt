package com.example.capstonandroid.activity

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstonandroid.R
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.Goal
import com.example.capstonandroid.network.dto.GoalResponse
import kotlinx.android.synthetic.main.activity_goal_run.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.*


class GoalRunActivity : AppCompatActivity() {


    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api


    private var startDate : String = ""
    private var endDate : String = ""
    private var title : String = ""
    private var event : String = ""
    private var goal : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_goal_run)

        initRetrofit()
        val sharedPreference = getSharedPreferences("other", MODE_PRIVATE)

//      이 타입이 디폴트 값
        var token = "Bearer " + sharedPreference.getString("TOKEN","")



        run_btnStartDate.setOnClickListener {

            val datepickercalendar = Calendar.getInstance()
            val year = datepickercalendar.get(Calendar.YEAR)
            val month = datepickercalendar.get(Calendar.MONTH)
            val day = datepickercalendar.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(
                this,
                { _, year, monthOfYear, dayOfMonth ->
//                  월이 0부터 시작하여 1을 더해주어야함
                    val month = monthOfYear + 1
//                   선택한 날짜의 요일을 구하기 위한 calendar
                    val calendar = Calendar.getInstance()
//                    선택한 날짜 세팅
                    calendar.set(year, monthOfYear, dayOfMonth)
                    val date = calendar.time
                    val simpledateformat = SimpleDateFormat("EEEE", Locale.getDefault())
                    val dayName: String = simpledateformat.format(date)



                    startDate = "$year-$month-$dayOfMonth"
                    println (startDate)
                },
                year,
                month,
                day
            )
//           최소 날짜를 현재 시각 이후로
            dpd.datePicker.minDate = System.currentTimeMillis() - 1000;
            dpd.show()
        }

        run_btnEndDate.setOnClickListener {

            val datepickercalendar = Calendar.getInstance()
            val year = datepickercalendar.get(Calendar.YEAR)
            val month = datepickercalendar.get(Calendar.MONTH)
            val day = datepickercalendar.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(
                this,
                { _, year, monthOfYear, dayOfMonth ->
//                  월이 0부터 시작하여 1을 더해주어야함
                    val month = monthOfYear + 1
//                   선택한 날짜의 요일을 구하기 위한 calendar
                    val calendar = Calendar.getInstance()
//                    선택한 날짜 세팅
                    calendar.set(year, monthOfYear, dayOfMonth)
                    val date = calendar.time
                    val simpledateformat = SimpleDateFormat("EEEE", Locale.getDefault())
                    val dayName: String = simpledateformat.format(date)



                    endDate = "$year-$month-$dayOfMonth"
                    println (endDate)
                },
                year,
                month,
                day
            )
//           최소 날짜를 현재 시각 이후로
            dpd.datePicker.minDate = System.currentTimeMillis() - 1000;
            dpd.show()
        }

        run_save.setOnClickListener {

            println("저장 눌렀어")
            println(startDate )
            println(endDate )
            title = run_title.text.toString()
            event = "R"
            goal = run_goal.text.toString().toInt()
            println()
            println()

            val Goal = Goal(
                    title = title,
                    goal = goal,
                    firstDate = startDate,
                    lastDate = endDate,
                    event = event
            )
            supplementService.goal(token,Goal).enqueue(object : Callback<GoalResponse>{
                override fun onResponse(
                    call: Call<GoalResponse>,
                    response: Response<GoalResponse>
                ) {
                    if(response.isSuccessful){
                        println("등록 완료 했습니다~")
                        finish()
                    }
                    else {
                        println("등록 실패 ㅋㅋ")
                    }
                }

                override fun onFailure(call: Call<GoalResponse>, t: Throwable) {
                }
            })
        }


    }

    override fun onStart() {
        super.onStart()

    }




    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }

}