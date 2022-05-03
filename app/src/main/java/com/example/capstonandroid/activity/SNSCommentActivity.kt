package com.example.capstonandroid.activity

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.R
import com.example.capstonandroid.adapter.RecyclerCommentAdapter
import com.example.capstonandroid.databinding.ActivitySnscommentBinding
import com.example.capstonandroid.fragment.HomeFragment
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.*
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
    private var postID = 0
    lateinit var binding: ActivitySnscommentBinding
    private var content = "";
    private var datasize = 0;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snscomment)

        binding = ActivitySnscommentBinding.inflate(layoutInflater)
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

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

//          content 값 받아오기
        println(content)

//        supplementService.SNSIndex(token, data_page).enqueue(object : Callback<GetPostsResponse>{
//            override fun onResponse(call: Call<GetPostsResponse>, response: Response<GetPostsResponse>) {
//                if(response.isSuccessful){
//                    println(response.body()!!.data[data_num].comment.size)
//                    postID = response.body()!!.data[data_num].id
//                    println("여기 포스트 아이디는?"+postID)
//                }else{
//                }
//            }
//
//            override fun onFailure(call: Call<GetPostsResponse>, t: Throwable) {
//                TODO("Not yet implemented")
//            }
//
//        })


//        supplementService.SNSIndex(token,data_page
//        ).enqueue(object : Callback<GetPostsResponse>{
//            override fun onResponse(
//                call: Call<GetPostsResponse>,
//                response: Response<GetPostsResponse>
//            ) {
//                if(response.isSuccessful) {
//
//                    println(response.body()!!.data[data_num].comment.size)
//                    for (i in 0..response.body()!!.data[data_num].comment.size-1) {
//                        list.add(
//                            CommentData(
//                                response.body()!!.data[data_num].comment[i].user.name.toString(),
//                                response.body()!!.data[data_num].comment[i].content,
//                                response.body()!!.data[data_num].comment[i].created_at,
//                                response.body()!!.data[data_num].comment[i].updated_at,
//                                response.body()!!.data[data_num].comment[i].id,
//                                response.body()!!.data[data_num].comment[i].user.profile
//
//                            )
//                        )
//                    }
//
//                    datasize = response.body()!!.data[data_num].comment.size
//                    binding.lstUser3.adapter = adapter
//                    binding.lstUser3.addItemDecoration(HomeFragment.DistanceItemDecorator(10))
//
//                    page++
//                }
//                else{
//                    println("실패함ㅋㅋ")
//                    println(response.body())
//                    println(response.message())
//                }
//            }
//
//            override fun onFailure(call: Call<GetPostsResponse>, t: Throwable) {
//                println("아예 가지도 않음ㅋㅋ")
//                println(t.message)
//            }
//        })

        binding.commitButton.setOnClickListener {
            var content = binding.content.text
//      객체 만들기
            val commentSend = CommentSend(
                content = content.toString(),
            )
            supplementService.commentSend(token,postID,commentSend).enqueue(object :
                Callback<CommentSendResponse> {
                override fun onResponse(
                    call: Call<CommentSendResponse>,
                    response: Response<CommentSendResponse>
                ) {



//                    supplementService.SNSIndex(token,data_page
//                    ).enqueue(object : Callback<GetPostsResponse> {
//                        override fun onResponse(
//                            call: Call<GetPostsResponse>,
//                            response: Response<GetPostsResponse>
//                        ) {
//                            if(response.isSuccessful) {
//
//                                datasize = response.body()!!.data[data_num].comment.size
//                                list.add(
//                                    CommentData(
//
//                                        response.body()!!.data[data_num].comment[datasize-1].user.name.toString(),
//                                        response.body()!!.data[data_num].comment[datasize-1].content,
//                                        response.body()!!.data[data_num].comment[datasize-1].created_at,
//                                        response.body()!!.data[data_num].comment[datasize-1].updated_at,
//                                        response.body()!!.data[data_num].comment[datasize-1].id,
//                                        response.body()!!.data[data_num].comment[datasize-1].user.profile
//                                    )
//                                )
//                                binding.lstUser3.adapter = adapter
//
//                                imm.hideSoftInputFromWindow(binding.content.getWindowToken(), 0);
//                                binding.content.setText(null)
//
//                                binding.lstUser3.smoothScrollToPosition(datasize)
//                            }
//                            else{
//                                println("실패함ㅋㅋ")
//                                println(response.body())
//                                println(response.message())
//                            }
//                        }
//
//                        override fun onFailure(call: Call<GetPostsResponse>, t: Throwable) {
//                            println("아예 가지도 않음ㅋㅋ")
//                            println(t.message)
//                        }
//                    })

                }

                override fun onFailure(call: Call<CommentSendResponse>, t: Throwable) {

                }
            })


        }

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
//
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