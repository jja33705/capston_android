package com.example.capstonandroid.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityMainBinding
import com.example.capstonandroid.fragment.HomeFragment
import com.example.capstonandroid.fragment.MeFragment
import com.example.capstonandroid.fragment.TrackFragment

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectExerciseKindDialog: Dialog // 커스텀 다이얼로그

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 커스텀 다이얼로그 초기화
        selectExerciseKindDialog = Dialog(this)
        selectExerciseKindDialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 제거
        selectExerciseKindDialog.setContentView(R.layout.select_kind_dialog)

        // 다이얼로그에서 라이딩 버튼 클릭했을 때
        val ridingButton: Button = selectExerciseKindDialog.findViewById(R.id.bt_riding)
        ridingButton.setOnClickListener{
            startRecordActivity("R")
        }

        // 다이얼로그에서 러닝 버튼 클릭했을 때
        val runningButton: Button = selectExerciseKindDialog.findViewById(R.id.bt_running)
        runningButton.setOnClickListener {
            startRecordActivity("B")
        }

        // 바텀 네비게이션에서 선택한 메뉴 아이디에 따라 표시할 화면 분기처리 (나중에 addToBackStack 부분 찾아보고 Transaction 관리해 줘야 할 것 같음.)
        binding.bottomNav.setOnItemSelectedListener {
            println(it.itemId)
            when (it.itemId) {
                R.id.homeFragment -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
                }
                R.id.trackFragment -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, TrackFragment()).commit()
                }
                R.id.recordActivity -> {
                    showSelectExerciseKindDialog()
                    return@setOnItemSelectedListener false
                }
                R.id.meFragment -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MeFragment()).commit()
                }
            }
            true
        }

        // 처음 들어왔을때는 homeFragment
        binding.bottomNav.selectedItemId = R.id.homeFragment

        // 레코드 중이면 레코드 액티비티로 이동
        if (getSharedPreferences("record", MODE_PRIVATE).getBoolean("isStarted", false)) {
            println("실행 중")

            val intent = Intent(this, RecordActivity::class.java)
            intent.putExtra("exerciseKind", getSharedPreferences("record", MODE_PRIVATE).getString("exerciseKind", "R"))
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) // 액티비티 스택 내에 있으면 재실행 함
            startActivity(intent)
        }
    }

    // 어떤 종류인지 선택하는 다이얼로그 띄움
    private fun showSelectExerciseKindDialog() {
        selectExerciseKindDialog.show()
        selectExerciseKindDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 모서리 둥글게 하기 위해서
    }

    // 선택한 운동 종류에 맞게 값을 넣고 실행
    private fun startRecordActivity(exerciseKind: String) {
        val intent = Intent(this, RecordActivity::class.java)
        intent.putExtra("exerciseKind", exerciseKind)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) // 액티비티 스택 내에 있으면 재실행 함
        startActivity(intent)
        selectExerciseKindDialog.dismiss() // 닫기
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
