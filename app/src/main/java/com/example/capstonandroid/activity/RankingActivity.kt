package com.example.capstonandroid.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityRankingBinding

class RankingActivity : AppCompatActivity() {

    private var _binding: ActivityRankingBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}