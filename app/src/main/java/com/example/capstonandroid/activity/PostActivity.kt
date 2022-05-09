package com.example.capstonandroid.activity

// SNS 누르면 자세히 뜨는 것

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstonandroid.R
import com.example.capstonandroid.Utils
import com.example.capstonandroid.adapter.CommentRecyclerViewAdapter
import com.example.capstonandroid.databinding.ActivityPostBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.*
import kotlinx.android.synthetic.main.activity_follower.*
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.activity_post.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class PostActivity : AppCompatActivity() {
    private lateinit var retrofit: Retrofit  //레트로핏
    private lateinit var supplementService: BackendApi // api

    private lateinit var post: Post

    private var _binding: ActivityPostBinding? = null
    private val binding get() = _binding!!

    private lateinit var commentRecyclerViewItemList: ArrayList<Comment?>
    private lateinit var commentRecyclerViewAdapter: CommentRecyclerViewAdapter
    private lateinit var commentRecyclerView: RecyclerView
    private var isLoading = false // 로딩 중인지
    private var isNext = false // 다음 페이지 있는지

    private var commentPage = 1      // 현재 페이지

    private var content = ""
    private var title = ""
    private var range = ""
    private var likeCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "活動"

        initRetrofit()



        }

        private fun initRetrofit() {
            retrofit = RetrofitClient.getInstance()
            supplementService = retrofit.create(BackendApi::class.java);
        }

        override fun onDestroy() {
            super.onDestroy()
            _binding = null
        }

        override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
            return super.onCreateView(name, context, attrs)

        }

    @SuppressLint("NewApi")
    override fun onStart() {
        super.onStart()


        val intent = intent
        var postId = intent.getIntExtra("postId", -1)
        var postKind = intent.getIntExtra("postKind",-1)

        var token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")


        var likeCheck = true

        if(postKind==0) {
            binding.deleteButton.visibility = View.GONE
            binding.range.visibility = View.GONE
            binding.edit.visibility = View.GONE
        }else {

            binding.deleteButton.visibility = View.VISIBLE
            binding.range.visibility = View.VISIBLE
            binding.edit.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.GONE
            binding.recyclerViewComment.visibility = View.GONE
        }

        CoroutineScope(Dispatchers.Main).launch {
            val getPostResponse = supplementService.getPost(token, postId)
            if (getPostResponse.isSuccessful) {
                post = getPostResponse.body()!!

                val defaultImage = R.drawable.map
                val mapImageUrl = post.img
                likeCheck = post.likeCheck

                Glide.with(this@PostActivity)
                    .load(mapImageUrl)
                    .placeholder(defaultImage)
                    .error(defaultImage)
                    .fallback(defaultImage)
                    .into(binding.imageViewMapImage)

                binding.title.setText( post.title)
                binding.content.setText (post.content)
                binding.time.text = Utils.timeToStringText(post.time)
                binding.calorie.text = "カロリー : ${post.calorie}Kcal"
                binding.averageSpeed.text = "平均速度 : ${post.average_speed}Km/h"
                binding.altitude.text = "累積高度 : ${String.format("%.0f", post.altitude)}m"
                binding.distance.text = "累積距離 : ${String.format("%.2f", post.distance)}Km"
                binding.username.text = post.user.name

                println(likeCheck.toString())
                if (likeCheck == true) {
                    binding.likeButton.setImageResource(R.drawable.like_new2)
                } else {
                    binding.likeButton.setImageResource(R.drawable.like_new3)
                }
                

                binding.like.text = "いいね！： ${post.likes.size}"

                title = post.title
                range = post.range
                content = post.content
                likeCount = post.likes.size
                if(range =="public"){
                    binding.range.setImageResource(R.drawable.lock1)
                }else {
                    binding.range.setImageResource(R.drawable.lock2)
                }
                
                val date = post.created_at // your date
                // date is already in Standard ISO format so you don't need custom formatted
                //                     val date = "2021-12-16T16:42:00.000000Z" // your date
                val dateTime: ZonedDateTime =
                    OffsetDateTime.parse(date).toZonedDateTime().plusHours(9)  // parsed date
                // format date object to specific format if needed
                val formatter =
                    DateTimeFormatter.ofPattern("yyyy年 MMM dd日 HH時 mm分 ", Locale.JAPANESE)
                println(dateTime.format(formatter).toString()) // output : Dec 16, 2021 16:42
                binding.createdate.text = dateTime.format(formatter).toString()

                binding.kind.text = when (post.kind) {
                    "싱글" -> "種類 : シングル"
                    "친선" -> "種類 : 練習"
                    "랭크" -> "種類 : ランク"
                    else -> "種類 : 自由"
                }

                if (post.opponent_post != null) {
                    binding.tvOpponent.text =
                        "${post.opponent_post!!.user.name}様の「${post.opponent_post!!.title}」と一緒に走りました！"
                    binding.tvOpponent.visibility = View.VISIBLE
                }
            }
        }




        CoroutineScope(Dispatchers.Main).launch {
            // 초기화
            commentPage = 1
            isNext = false
            isLoading = false
            commentRecyclerViewItemList = ArrayList()

            commentRecyclerView = binding.recyclerViewComment

            // 스크롤 리스너 등록
            commentRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    println("스트롤 함 $isNext $isLoading ${(recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()} ${commentRecyclerViewItemList.size}")
                    if (isNext) {
                        if (!isLoading) {
                            if (!recyclerView.canScrollVertically(1)) { // 최하단 끝까지 스크롤 했는지 감지
                                println("끝에 옴")
                                getMoreComments()
                                isLoading = true
                            }
                        }
                    }
                }
            })

            // 초기값 받아옴
            println("홈 프레그먼트$token")
            val getCommentIndexResponse =
                supplementService.commentIndex(token, postId, commentPage)
            if (getCommentIndexResponse.isSuccessful) {
                if (getCommentIndexResponse.body()!!.total == 0) {
                    isNext = false
                } else {
                    val commentList = getCommentIndexResponse.body()!!.data
                    println("여기이거뭐ㅐ" + getCommentIndexResponse.body().toString())
                    for (comment in commentList) {
                        commentRecyclerViewItemList.add(comment)
                    }
                    if (getCommentIndexResponse.body()!!.next_page_url != null) {
                        commentPage += 1
                        isNext = true
                    } else {
                        isNext = false
                    }
                }
            }
            commentRecyclerViewAdapter = CommentRecyclerViewAdapter(commentRecyclerViewItemList)
            commentRecyclerView.adapter = commentRecyclerViewAdapter

//             아이템 클릭 리스너 등록
            commentRecyclerViewAdapter.setOnItemClickListener(object :
                CommentRecyclerViewAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    Toast.makeText(this@PostActivity, "いいね", Toast.LENGTH_SHORT).show()
                }
            })
        }

        binding.edit.setOnClickListener{
            binding.like.visibility = View.GONE
            binding.likeButton.visibility = View.GONE
            binding.range.visibility = View.GONE
            binding.edit.visibility = View.GONE
            binding.deleteButton.visibility = View.GONE
            binding.editButton.visibility = View.VISIBLE
            binding.title.setFocusableInTouchMode (true)
            binding.title.setFocusable(true)
            binding.content.setFocusableInTouchMode (true)
            binding.content.setFocusable(true)
        }

        binding.editButton.setOnClickListener {

            binding.title.setFocusableInTouchMode (false)
            binding.title.setFocusable(false)
            binding.content.setFocusableInTouchMode(false)
            binding.content.setFocusable(false)

            CoroutineScope(Dispatchers.Main).launch {

                val update = Update(
                    content = binding.content.text.toString(),
                    title = binding.title.text.toString(),
                    range = range
                )
                val updateResponse =
                    supplementService.postUpdate(token, postId, update)

            }

                binding.like.visibility = View.VISIBLE
                binding.likeButton.visibility = View.VISIBLE
                binding.range.visibility = View.VISIBLE
                binding.edit.visibility = View.VISIBLE
                binding.deleteButton.visibility = View.VISIBLE
                binding.editButton.visibility = View.GONE
        }
        binding.likeButton.setOnClickListener {


            CoroutineScope(Dispatchers.Main).launch {
                val postLikeResponse = supplementService.postLike(token, postId)
                if (postLikeResponse.isSuccessful) {


                    if(likeCheck==true){
                        likeCount --
                        binding.like.text = "いいね！："+likeCount
                        likeCheck = false
                    }else {
                        likeCount ++
                        binding.like.text = "いいね！："+likeCount
                        likeCheck = true
                    }


                    if (likeCheck == true) {
                        binding.likeButton.setImageResource(R.drawable.like_new2)
                    } else {
                        binding.likeButton.setImageResource(R.drawable.like_new3)
                    }

                }
            }
//            supplementService.postLike(token, postID).enqueue(object : Callback<LikeResponse> {
//                override fun onResponse(
//                    call: Call<LikeResponse>,
//                    response: Response<LikeResponse>
//                ) {
//                    if (response.isSuccessful) {
//                        Toast.makeText(this@PostActivity, "いいねしました", Toast.LENGTH_SHORT).show()
////                        supplementService.SNSIndex(token,data_page).enqueue(object : Callback<GetPostsResponse> {
////                            override fun onResponse(call: Call<GetPostsResponse>, response: Response<GetPostsResponse>) {
////
////                                if(response.isSuccessful){
////                                    binding.like.setText("いいね！："+response.body()!!.data[data_num]!!.likes.size.toString())
////
////                                    if(response.body()!!.data[data_num].likeCheck===true){
////                                        binding.likeButton.setImageResource(R.drawable.like_new2)
////                                    }else{
////                                        binding.likeButton.setImageResource(R.drawable.like_new3)
////                                    }
////                                }  else{
////                                    println("실패함ㅋㅋ")
////                                    println(response.body())
////                                    println(response.message())
////                                }
////
////                            }
////
////                            override fun onFailure(call: Call<GetPostsResponse>, t: Throwable) {
////                                println("아예 가지도 않음ㅋㅋ")
////                                println(t.message)
////                            }
////                        })
//                    } else {
//
//                        Toast.makeText(this@PostActivity, "いいね", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
//                }
//
//            })
        }

        binding.commitButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                var commentSend = CommentSend(
                    content =  binding.commentContent.text.toString()
                )
                val commentSendResponse =
                    supplementService.commentSend(token, postId, commentSend)
                if (commentSendResponse.isSuccessful) {

                    println(commentSendResponse.message().toString())
//                        binding.like.text = "いいね！： ${post.likes.size-1}"
                }

                //                초기화 해준다 댓글창
                CoroutineScope(Dispatchers.Main).launch {
                    // 초기화
                    commentPage = 1
                    isNext = false
                    isLoading = false
                    commentRecyclerViewItemList = ArrayList()

                    commentRecyclerView = binding.recyclerViewComment

                    // 스크롤 리스너 등록
                    commentRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            println("스트롤 함 $isNext $isLoading ${(recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()} ${commentRecyclerViewItemList.size}")
                            if (isNext) {
                                if (!isLoading) {
                                    if (!recyclerView.canScrollVertically(1)) { // 최하단 끝까지 스크롤 했는지 감지
                                        println("끝에 옴")
                                        getMoreComments()
                                        isLoading = true
                                    }
                                }
                            }
                        }
                    })

                    // 초기값 받아옴
                    println("홈 프레그먼트$token")
                    val getCommentIndexResponse =
                        supplementService.commentIndex(token, postId, commentPage)
                    if (getCommentIndexResponse.isSuccessful) {
                        if (getCommentIndexResponse.body()!!.total == 0) {
                            isNext = false
                        } else {
                            val commentList = getCommentIndexResponse.body()!!.data
                            println("여기이거뭐ㅐ" + getCommentIndexResponse.body().toString())
                            for (comment in commentList) {
                                commentRecyclerViewItemList.add(comment)
                            }
                            if (getCommentIndexResponse.body()!!.next_page_url != null) {
                                commentPage += 1
                                isNext = true
                            } else {
                                isNext = false
                            }
                        }
                    }
                    commentRecyclerViewAdapter = CommentRecyclerViewAdapter(commentRecyclerViewItemList)
                    commentRecyclerView.adapter = commentRecyclerViewAdapter

//             아이템 클릭 리스너 등록
                    commentRecyclerViewAdapter.setOnItemClickListener(object :
                        CommentRecyclerViewAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            Toast.makeText(this@PostActivity, "いいね", Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                binding.commentContent.setText(null)
                softkeyboardHide()
            }

        }
//
//            supplementService.commentIndex(token,).enqueue(object : Callback<CommentIndexResponse>{
//                override fun onResponse(
//                    call: Call<CommentIndexResponse>,
//                    response: Response<CommentIndexResponse>
//                ) {
//                    if(response.isSuccessful){
//
//                    }else {
//
//                    }
//                }
//
//                override fun onFailure(call: Call<CommentIndexResponse>, t: Throwable) {
//                }
//
//            })


//
//            println("머지? 왜 안날라가")
//            val nextIntent = Intent(this, SNSCommentActivity::class.java)
//            nextIntent.putExtra("data_num", data_num)
//            nextIntent.putExtra("data_page", data_page)
//            startActivity(nextIntent)
//       binding.commitButton.setOnClickListener {
//           CoroutineScope(Dispatchers.Main).launch {
//               var commentSend = CommentSend(
//                   binding.content.text.toString()
//               )
//               val commentSendResponse =
//                   supplementService.commentSend(token, postId, commentSend)
//               if (commentSendResponse.isSuccessful) {
//
//                   println(commentSendResponse.message().toString())
////                        binding.like.text = "いいね！： ${post.likes.size-1}"
//               }
//           }
//       }


//        binding.followbutton.setOnClickListener {
//            supplementService.userFollow(token, userID).enqueue(object : Callback<FollowResponse> {
//                override fun onResponse(
//                    call: Call<FollowResponse>,
//                    response: Response<FollowResponse>
//                ) {
//                    binding.followbutton.setText("언팔로우")
//                }
//
//                override fun onFailure(call: Call<FollowResponse>, t: Throwable) {\
//                }
//            })
//        }
        binding.range.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                
                if(range=="private"){

                    val update = Update(
                        content = content,
                        title = title,
                        range = "public"
                    )
                    range = "public"
                    val updateResponse =
                        supplementService.postUpdate(token, postId, update)

                    if(updateResponse.isSuccessful){

                        binding.range.setImageResource(R.drawable.lock1)
                    }
                }else {

                    val update = Update(
                        content = content,
                        title = title,
                        range = "private"
                    )

                    range = "private"
                    val updateResponse =
                        supplementService.postUpdate(token, postId, update)

                    if(updateResponse.isSuccessful){
                        binding.range.setImageResource(R.drawable.lock2)
                    }
                }
            }
        }

        binding.deleteButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {

                val deleteResponse =
                    supplementService.postDelete(token, postId)

                if(deleteResponse.isSuccessful){
                    println("삭제했습니다")
                }
            }
            finish()
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }

    fun softkeyboardHide() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.commentContent.windowToken, 0)
    }

    private fun getMoreComments() {
        val intent = intent
        var postId = intent.getIntExtra("postId", -1)

        val runnable = Runnable {
            commentRecyclerViewItemList.add(null)
            commentRecyclerViewAdapter.notifyItemInserted(commentRecyclerViewItemList.size - 1)
        }
        commentRecyclerView.post(runnable)

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)

            commentRecyclerViewItemList.removeAt(commentRecyclerViewItemList.size - 1)
            commentRecyclerViewAdapter.notifyItemRemoved(commentRecyclerViewItemList.size)

//            val token = "Bearer " + Activity().getSharedPreferences("other", Context.MODE_PRIVATE).getString("TOKEN", "")!!

            var token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")
            val getCommentIndexResponse = supplementService.commentIndex(token,postId, commentPage)


            if (getCommentIndexResponse.isSuccessful) {
                val commentList = getCommentIndexResponse.body()!!.data
                for (comment in commentList) {
                    commentRecyclerViewItemList.add(comment)
                }

                commentRecyclerViewAdapter.updateItem(commentRecyclerViewItemList)
                commentRecyclerViewAdapter.notifyDataSetChanged()
                if (getCommentIndexResponse.body()!!.next_page_url != null) {
                    commentPage += 1
                    isNext = true
                } else {
                    isNext = false
                }
            }
        }
    }
    }
