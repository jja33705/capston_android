package com.example.capstonandroid.fragment

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.DistanceItemDecorator
import com.example.capstonandroid.R
import com.example.capstonandroid.activity.SNSDetailsActivity
import com.example.capstonandroid.adapter.RecyclerUserAdapter
import kotlinx.android.synthetic.main.fragment_me.*
import com.example.capstonandroid.databinding.FragmentHomeBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.SNSResponse
import com.example.capstonandroid.network.dto.UserData
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private  lateinit var  retrofit: Retrofit  //레트로핏
private  lateinit var supplementService: BackendApi // api

val indexnumber = ""

var SNSResponse  = {}

class MyActivity : AppCompatActivity() {
    // ...
    // 데이터를 담을 그릇 즉 배열



}

class HomeFragment : Fragment()  {


    private var param1: String? = null
    private var param2: String? = null

    // 바인딩 객체 타입에 ?를 붙여서 null을 허용 해줘야한다. ( onDestroy 될 때 완벽하게 제거를 하기위해 )
    private var Binding: FragmentHomeBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

        }





    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        val activity = activity as MainActivity?

//      함수 초기화
        initRetrofit()

        val sharedPreference = requireActivity().getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var token = "Bearer " + sharedPreference.getString("TOKEN","")
        println("홈 프레그먼트"+token)


        val list = ArrayList<UserData>()


//        lstUser.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                // 마지막 스크롤된 항목 위치
//                val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
//                // 항목 전체 개수
//                val itemTotalCount = recyclerView.adapter!!.itemCount - 1
//                if (lastVisibleItemPosition == itemTotalCount) {
//                    list.add(
//                        UserData(
//                            ContextCompat.getDrawable(
//                                requireContext(),
//                                R.drawable.sakai
//                            )!!,
//                        )
//                    )
//                }
//            }
//        })

        supplementService.SNSIndex(token).enqueue(object : Callback<SNSResponse>{
            override fun onResponse(
                call: Call<SNSResponse>,
                response: Response<SNSResponse>
            ) {



                if(response.isSuccessful) {


                    println(response.body()!!.data.size)

                    for (i in 0..response.body()!!.data.size-1) {
                        list.add(
                            UserData(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.sakai
                                )!!,
                                response.body()!!.data[i]!!.id,
                                response.body()!!.data[i].user.name,

                            )
                        )
                    }
                    val adapter = RecyclerUserAdapter(list, { data -> adapterOnClick(data) })
                    lstUser.adapter = adapter
                    lstUser.addItemDecoration(DistanceItemDecorator(10))




//                            var SNSResponse: SNSResponse? = response.body()
//
//                            println(SNSResponse!!.data[0].title)
//
//                            if (SNSResponse?.data?.count()==0){
//                                bindinghome.message.visibility = View.VISIBLE
//                                println("주행 한 기록이 없습니다.")
//                            }
//
//                            val items2 = mutableListOf<ListViewItem2>()
//
//                            var usersize: Int = SNSResponse!!.data.size
//                            println(usersize)
//
//                            for (i in 1..usersize) {
//                                items2.add(
//                                    ListViewItem2(
//                                        ContextCompat.getDrawable(
//                                            requireContext(),
//                                            R.drawable.sakai
//                                        )!!,
//                                        SNSResponse!!.data[i-1].title,
//                                        "작성일자 : " + SNSResponse!!.data[i-1].updated_at
//                                    )
//                                )
//
//                            }
//                            val adapter2 = ListViewAdapter2(items2)
//                            listView2.adapter = adapter2
//                            listView2.setOnItemClickListener { parent: AdapterView<*>, view: View, position: Int, id: Long ->
//                                val item2 = parent.getItemAtPosition(position) as ListViewItem2
//
//                                nextIntent.putExtra("number", position.toString())
//                                println(position.toString())
//                                startActivity(nextIntent)
//                            }
                }
//
//                    var user: User? = loginResponse!!.user
////                        데이터 클래스 USER 사용방법
////                        var user: User? = loginResponse!!.user
////                        print(user!!.birth)
//                    println(user!!.name)
//                    println(user!!.posts[0].title)
//
//                    val items = mutableListOf<ListViewItem>()
//
//
//                    println(user!!.posts[1].title)
////                    for (i in user!!.posts.)
//
//                    println(user!!.posts.size) //2
//                     var usersize : Int = user!!.posts.size
//                    for (i in usersize downTo 1){
//                        items.add(
//                            ListViewItem(ContextCompat.getDrawable(requireContext(),
//                            R.drawable.sakai)!!,
//                            user!!.posts[usersize-1].title,
//                            "작성일자 : " + user!!.posts[usersize-1].updated_at)
//                        )
//                            --usersize
//
//
//                    }
//
//                    val adapter = ListViewAdapter(items)
//                    listView.adapter = adapter
//                    listView.setOnItemClickListener { parent: AdapterView<*>, view: View, position: Int, id: Long -> val item = parent.getItemAtPosition(position) as ListViewItem
//                        Toast.makeText(requireContext(), item.title, Toast.LENGTH_SHORT).show() }
//
//                }
//                else{
//
//                    println("갔지만 실패")
//                    println(response.body())
//                    println(response.message())
//                    println(response.code())
//                }

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





    }



    private fun adapterOnClick(data: UserData) {
        Toast.makeText(requireContext(), "FunCall Clicked -> ID : ${data.id}, Name : ${data.name}", Toast.LENGTH_SHORT).show()
        println(data.id)



        val nextIntent = Intent(requireContext(), SNSDetailsActivity::class.java)
        nextIntent.putExtra("indexnumber", indexnumber)
        startActivity(nextIntent)

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

    override fun onDestroy() {
        Binding = null
        super.onDestroy()
    }

        companion object {
            /**
             * Use this factory method to create a new instance of
             * this fragment using the provided parameters.
             *
             * @param param1 Parameter 1.
             * @param param2 Parameter 2.
             * @return A new instance of fragment MeFragment.
             */
            // TODO: Rename and change types and number of parameters
            @JvmStatic
            fun newInstance(param1: String, param2: String) =
                MeFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
        }

    }
