package com.example.capstonandroid.activity

// 내기록 볼때 액티비티

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType.TYPE_NULL
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityMeDetailsBinding
import com.example.capstonandroid.databinding.ActivitySnsdetailsBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MeDetailsActivity : AppCompatActivity() {

    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    private var page = 1       // 현재 페이지

    lateinit var binding: ActivityMeDetailsBinding

    private var postID = 0
    private var content :String = ""
    private var range : String = ""
    private var title : String = ""

    private var time = 0.0;
    private var calorie : Double = 0.0;
    private var average_speed : Double= 0.0;
    private var altitude: Double = 0.0;
    private var distance: Double = 0.0;
    private var kind = "";
    
    private var username = "";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_me_details)

        binding = ActivityMeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRetrofit()
        val data_num: Int = intent.getIntExtra("data_num", 0)
        val data_page: Int = intent.getIntExtra("data_page", 0)



        println(data_page)
        val sharedPreference = getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var token = "Bearer " + sharedPreference.getString("TOKEN", "")
        println("여기는 좀 .. " + token)

        supplementService.myIndex(token, data_page).enqueue(object : Callback<MySNSResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<MySNSResponse>, response: Response<MySNSResponse>) {
                if (response.isSuccessful) {



                    val defaultImage = R.drawable.map
                    if(response.body()!!.data[data_num].map_image.size==0){
                        var url = ""
                        Glide.with(this@MeDetailsActivity)
                            .load(url) // 불러올 이미지 url
                            .placeholder(defaultImage) // 이미지 로딩 시작하기 전 표시할 이미지
                            .error(defaultImage) // 로딩 에러 발생 시 표시할 이미지
                            .fallback(defaultImage) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                            .into(binding.imageView) // 이미지를 넣을 뷰

                    }else {

                        val url = response.body()!!.data[data_num]!!.map_image[0].url
                        Glide.with(this@MeDetailsActivity)
                            .load(url) // 불러올 이미지 url
                            .placeholder(defaultImage) // 이미지 로딩 시작하기 전 표시할 이미지
                            .error(defaultImage) // 로딩 에러 발생 시 표시할 이미지
                            .fallback(defaultImage) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                            .into(binding.imageView) // 이미지를 넣을 뷰
                    }
                    println(response.body()!!.data[data_num]!!.title)

                    binding.title.setText(response.body()!!.data[data_num]!!.title)
                    binding.content.setText(response.body()!!.data[data_num]!!.content)

                    postID = response.body()!!.data[data_num].id
                    content = response.body()!!.data[data_num].content.toString()
                    range = response.body()!!.data[data_num].range.toString()
                    title = response.body()!!.data[data_num].title

                    time = response.body()!!.data[data_num]!!.time.toDouble()
                    calorie = response.body()!!.data[data_num]!!.calorie
                    average_speed = response.body()!!.data[data_num]!!.average_speed
                    altitude = response.body()!!.data[data_num]!!.altitude
                    distance = response.body()!!.data[data_num]!!.distance
                    kind = response.body()!!.data[data_num]!!.kind
                    username = response.body()!!.data[data_num]!!.user.name

                    val date = response.body()!!.data[data_num]!!.created_at // your date
// date is already in Standard ISO format so you don't need custom formatted
//                     val date = "2021-12-16T16:42:00.000000Z" // your date
                    val dateTime : ZonedDateTime = OffsetDateTime.parse(date).toZonedDateTime()  // parsed date
// format date object to specific format if needed
                    val formatter = DateTimeFormatter.ofPattern("yyyy年 MMM dd日 a HH時 mm分 ", Locale.JAPANESE)
                    println( dateTime.format(formatter).toString()) // output : Dec 16, 2021 16:42
//                     yyyy-MM-dd HH:mm:ss z

                     binding.createdate.setText(dateTime.format(formatter).toString())
                    if(time>3600){
                        binding.time.setText("時間 : "+time.toInt()/3600+"時間 "+time/60.toInt()+"分 "+time.toInt()%60+"秒")
                    }else if (time>60){
                        binding.time.setText("時間 : "+time.toInt()/60+"分 "+time.toInt()%60+"秒")
                    }else {
                        binding.time.setText("時間 : "+time.toInt()%60+"秒")
                    }

                    binding.content.setText(content)
                    binding.calorie.setText("カロリー : "+calorie+" Cal")
                    binding.kind.setText("種類 : " + kind)
                    binding.averageSpeed.setText("平均速度 : "+average_speed +" Km/h")
                    binding.altitude.setText("高度 : "+altitude)
                    binding.distance.setText("距離 : "+String.format("%.2f",distance/1000)+" Km")

                    println("콘텐츠"+content)
                    println("랭스"+range)
                    if(range=="private"){
                        binding.range.setImageResource(R.drawable.lock)}
                    else{
                        binding.range.setImageResource(R.drawable.lockaa)
                    }
                } else {
                    println("실패함ㅋㅋ")
                    println(response.body())
                    println(response.message())
                }
            }

            override fun onFailure(call: Call<MySNSResponse>, t: Throwable) {

            }
        })
        //      객체 만들기

//        binding.backButton.setOnClickListener {
//            finish()
//        }

        binding.deleteButton.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("本当に削除しますか。")
                .setPositiveButton("はい", DialogInterface.OnClickListener{ dialog,id->
                    println("삭제하기전 페이지?? 뭘까요ㅋㅋ"+postID)
                    supplementService.postDelete(token,postID).enqueue(object : Callback<DeleteResponse> {
                        override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                            intent.putExtra("resultData","world")
                            setResult(RESULT_OK,intent)
                            finish()
                        }
                        override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {

                        }
                    })
                })
                .setNegativeButton("いいえ",DialogInterface.OnClickListener{ dialog,id ->
                    println("취소 하셨네요")
                })

            builder.show()

        }


        binding.range.setOnClickListener{

            val rangeName :String
             if (range=="public"){
                 rangeName = "非公開"
             }else {
                 rangeName = "公開"
             }
            val builder = AlertDialog.Builder(this)
            builder.setTitle("本当に" +  rangeName +"しますか。")
                .setPositiveButton("はい", DialogInterface.OnClickListener{ dialog,id->

                    if(range=="public"){
                        val update = Update(
                            title = title,
                            content = content,
                            range = "private"
                        )

                        println(update)
                        supplementService.postUpdate(token,postID,update).enqueue(object : Callback<Int>{
                            override fun onResponse(
                                call: Call<Int>,
                                response: Response<Int>
                            ) {


                            }

                            override fun onFailure(call: Call<Int>, t: Throwable) {

                                println("실패")
                            }

                        })
                        range = "private"
                        binding.range.setImageResource(R.drawable.lock)
                    }else {
                        val update = Update(
                            title = title,
                            content = content,
                            range = "public"
                        )

                        println(update)
                        supplementService.postUpdate(token,postID,update).enqueue(object : Callback<Int>{
                            override fun onResponse(
                                call: Call<Int>,
                                response: Response<Int>
                            ) {

                            }

                            override fun onFailure(call: Call<Int>, t: Throwable) {

                                println("실패")
                            }

                        })
                        range = "public"
                        binding.range.setImageResource(R.drawable.lockaa)
                    }
                })
                .setNegativeButton("いいえ",DialogInterface.OnClickListener{ dialog,id ->
                    println("취소 하셨네요")
                })

            builder.show()









        }

//        binding.content.setInputType(TYPE_NULL);
//        binding.title.setInputType(TYPE_NULL)
        binding.commentButton.setOnClickListener {
            println("머지? 왜 안날라가")
            val nextIntent = Intent(this, MeCommentActivity::class.java)
            nextIntent.putExtra("data_num", data_num)
            nextIntent.putExtra("data_page", data_page)
            startActivity(nextIntent)
        }

        binding.edit.setOnClickListener{
            binding.edit.visibility = View.GONE
            binding.title.setFocusableInTouchMode (true);
            binding.title.setFocusable(true);
            binding.content.setFocusableInTouchMode (true);
            binding.content.setFocusable(true);
//            binding.backButton.visibility = View.GONE
            binding.deleteButton.visibility = View.GONE
            binding.editButton.visibility = View.VISIBLE

        }

        binding.editButton.setOnClickListener {


            val builder = AlertDialog.Builder(this)
            builder.setTitle("本当に修正しますか。")
                .setPositiveButton("はい", DialogInterface.OnClickListener{ dialog,id->
                    title = binding.title.text.toString()
                    content = binding.content.text.toString()

                    val update = Update(
                        title = title,
                        content = content,
                        range = "private"
                    )
                    supplementService.postUpdate(token,postID,update).enqueue(object : Callback<Int>{
                        override fun onResponse(call: Call<Int>, response: Response<Int>) {
                            if (response.isSuccessful){

                                binding.edit.visibility = View.VISIBLE
                                binding.title.setFocusableInTouchMode (false);
                                binding.title.setFocusable(false);
                                binding.content.setFocusableInTouchMode (false);
                                binding.content.setFocusable(false);
//            binding.backButton.visibility = View.GONE
                                binding.deleteButton.visibility = View.VISIBLE
                                binding.editButton.visibility = View.GONE
                            }
                            else{}
                        }

                        override fun onFailure(call: Call<Int>, t: Throwable) {
                            TODO("Not yet implemented")
                        }
                    })
                })
                .setNegativeButton("いいえ",DialogInterface.OnClickListener{ dialog,id ->
                    println("취소 하셨네요")
                })

            builder.show()




        }
    }



        private fun initRetrofit() {
            retrofit = RetrofitClient.getInstance()
            supplementService = retrofit.create(BackendApi::class.java);
        }


}