package com.example.capstonandroid

import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BaeminRepository {
    //private val parameter: MutableMap<String, String> = HashMap()
    var _notice = MutableLiveData<Data>()

    fun loadBaeminNotice(page: Int) {
//        parameter["page"] = page.toString()
        val call = BaeminApi.createApi().loadNotice(page.toString())

        call.enqueue(object : Callback<Baemin> {
            override fun onResponse(
                call: Call<Baemin>,
                response: Response<Baemin>
            ) {
                if (response.isSuccessful) {
                    _notice.value = response.body()!!.data
                }
            }

            override fun onFailure(call: Call<Baemin>, t: Throwable) {

            }
        })
    }
}