package com.example.capstonandroid.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import com.example.capstonandroid.databinding.ActivityCountDownBinding

class CountDownActivity : AppCompatActivity() {
    private var _binding: ActivityCountDownBinding? = null
    private val binding get() = _binding!!

    private lateinit var countDownTextViewThree: TextView
    private lateinit var countDownTextViewTwo: TextView
    private lateinit var countDownTextViewOne: TextView

    private var size = 0F // 세로 멈출 사이즈

    companion object {
        const val COUNT_DOWN_ACTIVITY_RESULT_CODE = 111
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityCountDownBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 텍스트뷰 초기화
        countDownTextViewThree = binding.tvCountDownThree
        countDownTextViewTwo = binding.tvCountDownTwo
        countDownTextViewOne = binding.tvCountDownOne

        //세로높이 구함
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val windowMetrics = windowManager.currentWindowMetrics
        val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        size = ((windowMetrics.bounds.height() - insets.bottom - insets.top) / 2).toFloat() - 70F

        showAnimation(countDownTextViewThree)


    }

    private fun showAnimation(textView: TextView) {
        println("시작됨 ${textView.text}")
        ObjectAnimator.ofFloat(textView, "translationY", size).apply {
            duration = 700 // 소요시간

            // 끝났을때 콜백 달아줌
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    textView.visibility = View.GONE

                    Thread.sleep(300)

//                     텍스트에 따라 분기처리
                    when (textView.text) {
                        "3" -> {
                            showAnimation(countDownTextViewTwo)
                        }
                        "2" -> {
                            showAnimation(countDownTextViewOne)
                        }
                        "1" -> {
                            setResult(COUNT_DOWN_ACTIVITY_RESULT_CODE)
                            finish()
                        }
                    }
                }
            })
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}