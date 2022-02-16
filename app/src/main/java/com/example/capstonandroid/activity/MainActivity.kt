package com.example.capstonandroid.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityMainBinding
import com.example.capstonandroid.fragment.HomeFragment
import com.example.capstonandroid.fragment.MeFragment
import com.example.capstonandroid.fragment.TrackFragment

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    val intent: Intent = Intent(this, RecordActivity::class.java)
                    startActivity(intent)
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
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
