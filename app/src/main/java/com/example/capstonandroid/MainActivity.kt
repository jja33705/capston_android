package com.example.capstonandroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.capstonandroid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 바텀 네비게이션에서 선택한 메뉴 아이디에 따라 표시할 화면 분기처리
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

        // 처음 들어왔을때는 homefragment
        binding.bottomNav.selectedItemId = R.id.homeFragment
    }
//    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
