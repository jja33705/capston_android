package com.example.capstonandroid

import android.app.Application
import androidx.multidex.MultiDexApplication

class MyApplication : Application() {
    companion object {
    lateinit var prefs: PreferenceUtil
    }

    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate()
    }
}


