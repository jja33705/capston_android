package com.example.capstonandroid.fragment

import ListViewAdapter2
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.R
import com.example.capstonandroid.activity.MainActivity
import com.example.capstonandroid.activity.SNSDetailsActivity
import com.example.capstonandroid.databinding.ActivityMainBinding
import com.example.capstonandroid.databinding.FragmentHomeBinding
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.dto.*
import com.example.capstonandroid.MainViewModel
import com.example.capstonandroid.NoticeAdapter
import com.example.capstonandroid.adapter.CustomAdapter
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
/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class MyActivity : AppCompatActivity() {
    // ...
}

class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentHomeBinding? = null

    private lateinit var model: MainViewModel
    private lateinit var noticeAdapter: NoticeAdapter
    private var page = 1

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

        initRetrofit()


        val bindinghome = FragmentHomeBinding.bind(view)

        val activity = activity as MainActivity?

        val nextIntent = Intent(requireContext(), SNSDetailsActivity::class.java)

//      함수 초기화
        initRetrofit()

        val sharedPreference = requireActivity().getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var token = "Bearer " + sharedPreference.getString("TOKEN","")
        println("홈 프레그먼트"+token)



                supplementService.SNSIndex(token).enqueue(object : Callback<SNSResponse>{
                    override fun onResponse(
                        call: Call<SNSResponse>,
                        response: Response<SNSResponse>
                    ) {

                        if(response.isSuccessful) {
                            println(response.body())
                            var userList = arrayListOf<DataVo>(

                                DataVo("아이유", "test1", "전주시", 30000000,"user_img_01"),
                                DataVo("홍길동", "test2", "김해시", 50000000,"user_img_02"),
                                DataVo("김", "test3", "안동시", 70000000,"user_img_03"),
                                DataVo("이", "test4", "성주군", 90000000,"user_img_04"),

                            )

                            val mAdapter = CustomAdapter(requireContext(),userList)
                            recycler_view.adapter = mAdapter

                            val layout = LinearLayoutManager(requireContext())
                            recycler_view.layoutManager = layout
                            recycler_view.setHasFixedSize(true)
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
                    }
                })

        model = ViewModelProvider(this).get(MainViewModel::class.java)

        model.loadBaeminNotice(page)
        bindinghome.rvBaeminNotice.apply {
            bindinghome.rvBaeminNotice.layoutManager = LinearLayoutManager(context)
            noticeAdapter = NoticeAdapter()
            bindinghome.rvBaeminNotice.adapter = noticeAdapter
        }

        model.getAll().observe(requireActivity(), Observer{
            noticeAdapter.setList(it.content)
            noticeAdapter.notifyItemRangeInserted((page - 1) * 10, 10)
        })

        bindinghome.rvBaeminNotice.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastVisibleItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                val itemTotalCount = recyclerView.adapter!!.itemCount-1

                if (bindinghome.rvBaeminNotice.canScrollVertically(1) && lastVisibleItemPosition == itemTotalCount) {
                    noticeAdapter.deleteLoading()
                    model.loadBaeminNotice(++page)
                }
            }
        })



    }
//        binding..setOnClickListener {
//            val intent = Intent(this, SubActivity::class.java)
//            startActivity(intent)
//        }




    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }

    override fun onDestroy() {
        _binding = null
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