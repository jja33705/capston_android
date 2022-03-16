package com.example.capstonandroid

import androidx.multidex.MultiDexApplication

class MyApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
//        val sharedPreference = getSharedPreferences("other", 0)
//        println(sharedPreference.getString("TOKEN",""))
//        println("이건 언제 실행이 될까요???????????????? ")
    }
}
