package com.example.capstonandroid

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.capstonandroid.BaeminRepository
import com.example.capstonandroid.Data

class MainViewModel : ViewModel() {
    private val baeminRepository = BaeminRepository()
    private val notice: LiveData<Data>
        get() = baeminRepository._notice

    fun loadBaeminNotice(page: Int){
        baeminRepository.loadBaeminNotice(page)
    }

    fun getAll(): LiveData<Data> {
        return notice
    }
}