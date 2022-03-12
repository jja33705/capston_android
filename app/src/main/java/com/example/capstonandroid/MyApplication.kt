package com.example.capstonandroid

import androidx.multidex.MultiDexApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MyApplication : MultiDexApplication() {
    companion object {
        lateinit var auth: FirebaseAuth
        var email: String? = null

        fun checkAuth(): Boolean {
            val currentUser = auth.currentUser
            return currentUser?.let {
                email = currentUser.email
                currentUser.isEmailVerified
            } ?: let {
                false
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        val sharedPreference = getSharedPreferences("other", 0)
        println(sharedPreference.getString("TOKEN",""))
        println("이건 언제 실행이 될까요???????????????? ")
    }
}
