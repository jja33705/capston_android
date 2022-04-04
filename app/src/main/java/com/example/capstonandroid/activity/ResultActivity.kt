package com.example.capstonandroid.activity

//결과 액티비티

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private var _binding: ActivityResultBinding? = null
    private val binding: ActivityResultBinding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {


        _binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        binding.resultSNSupload.setOnClickListener {
            // 다이얼로그를 생성하기 위해 Builder 클래스 생성자를 이용해 줍니다.
            val builder = AlertDialog.Builder(this)
            builder.setTitle("타이틀 입니다.")
                .setMessage("메세지 내용 부분 입니다.")
                .setPositiveButton("확인",
                    DialogInterface.OnClickListener { dialog, id ->
                    })
                .setNegativeButton("취소",
                    DialogInterface.OnClickListener { dialog, id ->
                    })
            // 다이얼로그를 띄워주기
            builder.show()
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}