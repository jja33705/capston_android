package com.example.capstonandroid.activity

import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.R
import com.example.capstonandroid.adapter.RecyclerCommentAdapter
import com.example.capstonandroid.adapter.RecyclerUserAdapter
import com.example.capstonandroid.databinding.ActivitySnscommentBinding
import com.example.capstonandroid.databinding.ActivitySnsdetailsBinding
import com.example.capstonandroid.fragment.HomeFragment
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.Comment
import com.example.capstonandroid.network.dto.CommentData
import com.example.capstonandroid.network.dto.SNSResponse
import com.example.capstonandroid.network.dto.UserData
import kotlinx.android.synthetic.main.activity_snscomment.*
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class SNSCommentActivity : AppCompatActivity()
{
    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api


    private var page = 1      // 현재 페이지
    object user {}
    lateinit var binding: ActivitySnscommentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snscomment)

        binding = ActivitySnscommentBinding.inflate(layoutInflater)


        setContentView(binding.root)
        initRetrofit()

        val data_num : Int = intent.getIntExtra("data_num",0)
        val data_page : Int = intent.getIntExtra("data_page",0)

        page = 1       // 현재 페이지

        val sharedPreference = getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var token = "Bearer " + sharedPreference.getString("TOKEN","")
        println(token)

        val list = ArrayList<CommentData>()
        val adapter = RecyclerCommentAdapter(list, { data -> adapterOnClick(data) })

        supplementService.SNSIndex(token, data_page).enqueue(object : Callback<SNSResponse>{
            override fun onResponse(call: Call<SNSResponse>, response: Response<SNSResponse>) {
                if(response.isSuccessful){
                    println(response.body()!!.data[data_num].comment.size)
                }else{
                }
            }

            override fun onFailure(call: Call<SNSResponse>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })


        supplementService.SNSIndex(token,data_page
        ).enqueue(object : Callback<SNSResponse>{
            override fun onResponse(
                call: Call<SNSResponse>,
                response: Response<SNSResponse>
            ) {
                if(response.isSuccessful) {

                    println(response.body()!!.data[data_num].comment.size)
                    for (i in 0..response.body()!!.data[data_num].comment.size-1) {
                        list.add(
                            CommentData(
                                response.body()!!.data[data_num].comment[i].user.name.toString(),
                                response.body()!!.data[data_num].comment[i].content,
                                response.body()!!.data[data_num].comment[i].created_at,
                                response.body()!!.data[data_num].comment[i].updated_at,
                            )
                        )
                    }
                    lstUser3.adapter = adapter
                    lstUser3.addItemDecoration(HomeFragment.DistanceItemDecorator(10))

                    page++
                }
                else{
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


//        binding.lstUser3.addOnScrollListener(object : RecyclerView.OnScrollListener(){
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                if (!lstUser3.canScrollVertically(1)){
//
//                    supplementService.SNSIndex(token, page
//                    ).enqueue(object : Callback<SNSResponse>{
//                        override fun onResponse(
//                            call: Call<SNSResponse>,
//                            response: Response<SNSResponse>
//                        ) {
//
//                            if(response.isSuccessful&&response.body()!!.data[data_num].comment.size==0) {
//                                for (i in 0..response.body()!!.data[data_num].comment.size-1) {
//                                    list.add(
//                                        CommentData(
//                                            response.body()!!.data[data_num].comment[i].user.name.toString(),
//                                            response.body()!!.data[data_num].comment[i].content,
//                                            response.body()!!.data[data_num].comment[i].created_at,
//                                            response.body()!!.data[data_num].comment[i].updated_at,
//                                        )
//                                    )
//                                }
////                                lstUser.adapter!!.notifyItemInserted(10)
//                                lstUser3.adapter!!.notifyDataSetChanged()
//
//                                page++
//                            }else{
//
//                            }
//                        }
//
//                        override fun onFailure(call: Call<SNSResponse>, t: Throwable) {
//                            TODO("Not yet implemented")
//                        }
//                    })
//
//                }
//            }
//        })
    }

    private fun adapterOnClick(data: CommentData) {


    }
    class DistanceItemDecorator(private val value: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)

            outRect.top = value
            outRect.left = value
            outRect.bottom = value
            outRect.right = value
        }
    }

    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }

}