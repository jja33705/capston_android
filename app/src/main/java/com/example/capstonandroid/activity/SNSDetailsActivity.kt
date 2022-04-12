package com.example.capstonandroid.activity

// SNS 누르면 자세히 뜨는 것

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.viewpager2.widget.ViewPager2
import com.example.capstonandroid.R
import com.example.capstonandroid.adapter.ViewPagerAdapter
import com.example.capstonandroid.databinding.*
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.LikeResponse
import com.example.capstonandroid.network.dto.SNSResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class SNSDetailsActivity : AppCompatActivity() {
    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    private var page = 0       // 현재 페이지


    private var time = 0.0;
    private var calorie : Double = 0.0;
    private var average_speed : Double= 0.0;
    private var altitude: Double = 0.0;
    private var distance: Double = 0.0;
    private var kind = "";
    private var content = "";

    lateinit var binding: ActivitySnsdetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snsdetails)


        binding = ActivitySnsdetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "활동"

        initRetrofit()

//        if (intent.hasExtra("indexnumber")) {
//            binding.textView10.text = intent.getStringExtra("indexnumber")
//            /* "nameKey"라는 이름의 key에 저장된 값이 있다면
//               textView의 내용을 "nameKey" key에서 꺼내온 값으로 바꾼다 */
//
//        } else {
//            Toast.makeText(this, "전달된 이름이 없습니다", Toast.LENGTH_SHORT).show()
//        }
//      누른 indexnumber~ 받아오기

        var postID = 0
        val data_num : Int = intent.getIntExtra("data_num",0)
        val data_page : Int = intent.getIntExtra("data_page",0)

        println(data_num.toString())
        println(data_page.toString())
        val sharedPreference = getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var token = "Bearer " + sharedPreference.getString("TOKEN","")
        println(token)


         supplementService.SNSIndex(token,data_page).enqueue(object : Callback<SNSResponse> {
             override fun onResponse(call: Call<SNSResponse>, response: Response<SNSResponse>) {

                 if(response.isSuccessful){

                    binding.title.setText(response.body()!!.data[data_num]!!.title)
//                     binding.content.setText(response.body()!!.data[data_num]!!.content)
//                     println(response.body()!!.data[data_num]!!.likes.size)
                    binding.like.setText("좋아요 갯수 : "+response.body()!!.data[data_num]!!.likes.size.toString())
                     postID = response.body()!!.data[data_num]!!.id
                     println(postID)

                     time = response.body()!!.data[data_num]!!.time.toDouble()
                     calorie = response.body()!!.data[data_num]!!.calorie
                     average_speed = response.body()!!.data[data_num]!!.average_speed
                     altitude = response.body()!!.data[data_num]!!.altitude
                     distance = response.body()!!.data[data_num]!!.distance
                     kind = response.body()!!.data[data_num]!!.kind
                     content = response.body()!!.data[data_num]!!.content

                     binding.content.setText(content)
                     binding.time.setText("시간 : "+time)
                     binding.calorie.setText("칼로리 : "+calorie)
                     binding.kind.setText("종류 : " + kind)
                     binding.averageSpeed.setText("평균 속도 : "+average_speed)
                     binding.altitude.setText("고도 : "+altitude)
                     binding.distance.setText("거리 : "+distance)
                 }  else{
                     println("실패함ㅋㅋ")
                     println(response.body())
                     println(response.message())
                 }
             }

             override fun onFailure(call: Call<SNSResponse>, t: Throwable) {
                 println("아예 가지도 않음ㅋㅋ")
                 println(t.message)
             }
         })

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.likeButton.setOnClickListener{
            supplementService.postLike(token,postID).enqueue(object : Callback<LikeResponse>{
                override fun onResponse(
                    call: Call<LikeResponse>,
                    response: Response<LikeResponse>
                ) {
                    if (response.isSuccessful){
                        Toast.makeText(this@SNSDetailsActivity,"좋아요 눌렀습니다.",Toast.LENGTH_SHORT).show()
                        supplementService.SNSIndex(token,data_page).enqueue(object : Callback<SNSResponse> {
                            override fun onResponse(call: Call<SNSResponse>, response: Response<SNSResponse>) {

                                if(response.isSuccessful){
                                    binding.like.setText("좋아요 갯수 : "+response.body()!!.data[data_num]!!.likes.size.toString())

                                }  else{
                                    println("실패함ㅋㅋ")
                                    println(response.body())
                                    println(response.message())
                                }
                            }

                            override fun onFailure(call: Call<SNSResponse>, t: Throwable) {
                                println("아예 가지도 않음ㅋㅋ")
                                println(t.message)
                            }
                        })
                    }else {

                        Toast.makeText(this@SNSDetailsActivity,"좋아요 실패했습니다.",Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })
        }

        binding.commentButton.setOnClickListener {

            println("머지? 왜 안날라가")
            val nextIntent = Intent(this, SNSCommentActivity::class.java)
            nextIntent.putExtra("data_num", data_num)
            nextIntent.putExtra("data_page", data_page)
            startActivity(nextIntent)
        }


}
    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }



}