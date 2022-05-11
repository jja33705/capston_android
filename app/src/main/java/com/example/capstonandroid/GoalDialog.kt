package com.example.capstonandroid

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.capstonandroid.databinding.GoalAlertDialogBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.Goal
import com.example.capstonandroid.network.dto.GoalResponse
import kotlinx.android.synthetic.main.activity_goal_bike.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.*

class GoalDialog(
    context: Context,
    private val okCallback: (String) -> Unit,
) : Dialog(context) { // 뷰를 띄워야하므로 Dialog 클래스는 context를 인자로 받는다.

    private var startDate : String = ""
    private var endDate : String = ""
    private var titleSet : String = ""
    private var event : String = ""
    private var goalSet : Int = 0


    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    private lateinit var binding: GoalAlertDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 만들어놓은 dialog_profile.xml 뷰를 띄운다.
        binding = GoalAlertDialogBinding.inflate(layoutInflater)
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


         event = MyApplication.prefs.getString("eventType", "")

        if(event == "B"){
            binding.title.hint = "자전거 목표 제목"
        }else {

            binding.title.hint = "달리기 목표 제목"
        }
        binding.btnStartDate.setOnClickListener {
            val datepickercalendar = Calendar.getInstance()
            val year = datepickercalendar.get(Calendar.YEAR)
            val month = datepickercalendar.get(Calendar.MONTH)
            val day = datepickercalendar.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(
                context,
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

                },
                year,
                month,
                day
            )
//           최소 날짜를 현재 시각 이후로
            dpd.datePicker.minDate = System.currentTimeMillis() - 1000;
            dpd.show() }

        binding.btnEndDate.setOnClickListener {
            val datepickercalendar = Calendar.getInstance()
            val year = datepickercalendar.get(Calendar.YEAR)
            val month = datepickercalendar.get(Calendar.MONTH)
            val day = datepickercalendar.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(
                context,
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

                },
                year,
                month,
                day
            )
//           최소 날짜를 현재 시각 이후로
            dpd.datePicker.minDate = System.currentTimeMillis() - 1000;
            dpd.show()
        }

        binding.save.setOnClickListener {
            var token = "Bearer " +MyApplication.prefs.getString("TOKEN", "")
            println("저장 눌렀어")
            println(startDate )
            println(endDate )
            titleSet = binding.title.text.toString()
            goalSet = binding.goal.text.toString().toInt()
            println()
            println()

            val Goal = Goal(
                title = titleSet,
                goal = goalSet,
                firstDate = startDate,
                lastDate = endDate,
                event = event
            )
            supplementService.goal(token,Goal).enqueue(object : Callback<GoalResponse> {
                override fun onResponse(
                    call: Call<GoalResponse>,
                    response: Response<GoalResponse>
                ) {
                    if(response.isSuccessful){
                        println("등록 완료 했습니다~")
                        dismiss()
                        okCallback("Hello")

                    }
                    else {
                        println("등록 실패 ㅋㅋ")
                    }
                }

                override fun onFailure(call: Call<GoalResponse>, t: Throwable) {
                }
            })

        }

        binding.back.setOnClickListener {
            dismiss()
        }
    }


    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }

}