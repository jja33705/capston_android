package com.example.capstonandroid.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {

    private var _binding: ActivityNotificationBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "通知"


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}