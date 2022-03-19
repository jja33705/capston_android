package com.example.capstonandroid.fragment

import ListViewAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.FragmentActivityMeBinding
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.dto.*
import kotlinx.android.synthetic.main.fragment_activity_me.*
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

class ActivityMeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // 바인딩 객체 타입에 ?를 붙여서 null을 허용 해줘야한다. ( onDestroy 될 때 완벽하게 제거를 하기위해 )
    private var mBinding: FragmentActivityMeBinding? = null
    // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!

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
        mBinding = FragmentActivityMeBinding.inflate(inflater, container, false)


        // Inflate the layout for this fragment


        var user_name = ""
        var user_followers = ""
        var user_followings = ""
        var user_mmr : Int= 0
//        함수 초기화
        initRetrofit()
////      토큰 불러오기

        val sharedPreference = requireActivity().getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var TOKEN = "Bearer " + sharedPreference.getString("TOKEN","")
        println("프로필 미 프래그먼트 + "+TOKEN)

        supplementService.userGet(TOKEN.toString()).enqueue(object :Callback<LoginUserResponse>
        {
            override fun onResponse(call: Call<LoginUserResponse>, response: Response<LoginUserResponse>) {
                println(response.body())
        if(response.isSuccessful) {
            println(response.body())

            var LoginUserResponse: LoginUserResponse? = response.body()

//            println("여긴뭐여?"+LoginUserResponse!!.posts[0].title)


            val items = mutableListOf<ListViewItem>()

            println(LoginUserResponse?.posts?.size)

            var usersize: Int = LoginUserResponse!!.posts.size

            if (usersize==0){
                binding.message.visibility = View.VISIBLE
                println("주행 한 기록이 없습니다.")
            }

            println(usersize)
            for (i in usersize downTo 1) {
                items.add(
                    ListViewItem(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.sakai
                        )!!,
                        LoginUserResponse!!.posts[usersize - 1].title,
                        "작성일자 : " + LoginUserResponse!!.posts[usersize - 1].updated_at
                    )
                )
                --usersize


            }
            val adapter = ListViewAdapter(items)
            listView.adapter = adapter
            listView.setOnItemClickListener { parent: AdapterView<*>, view: View, position: Int, id: Long ->
                val item = parent.getItemAtPosition(position) as ListViewItem

            }
        }else{

        }

//
            }

            override fun onFailure(call: Call<LoginUserResponse>, t: Throwable) {

            }

        })



//        supplementService.myIndex(TOKEN.toString()).enqueue(object :Callback<IndexResponse>{
//            override fun onResponse(call: Call<IndexResponse>, response: Response<IndexResponse>) {
//
//                if(response.isSuccessful) {
//                    println(response.body())
//
//                    var myIndex: IndexResponse? = response.body()
//
////                            var data: Data? =
//
//                    val items = mutableListOf<ListViewItem>()
//
//                    var usersize: Int = myIndex!!.data.size
//                    println(usersize)
//                    for (i in usersize downTo 1) {
//                        items.add(
//                            ListViewItem(
//                                ContextCompat.getDrawable(
//                                    requireContext(),
//                                    R.drawable.sakai
//                                )!!,
//                                myIndex!!.data[usersize - 1].title,
//                                "작성일자 : " + myIndex!!.data[usersize - 1].updated_at
//                            )
//                        )
//                        --usersize
//
//
//                    }
//                    val adapter = ListViewAdapter(items)
//                    listView.adapter = adapter
//                    listView.setOnItemClickListener { parent: AdapterView<*>, view: View, position: Int, id: Long ->
//                        val item = parent.getItemAtPosition(position) as ListViewItem
//                        Toast.makeText(requireContext(), item.title, Toast.LENGTH_SHORT).show()
//                    }
//                }}
//            override fun onFailure(call: Call<IndexResponse>, t: Throwable) {
//                TODO("Not yet implemented")
//            }
//
//        })

//        supplementService.loginPost(login).enqueue(object : Callback<LoginResponse>{
//            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
//
//
//                if(response.isSuccessful){
//                    println("성공 활동 기록 프래그먼트")
////
//                    println("콜백 응답으로 온것"+ response.body())
//
//
//                    var loginResponse: LoginResponse? = response.body()
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
//
//            }
//
//            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
//                TODO("Not yet implemented")
//                println("실패")
//                println(t.message)
//            }
//
//        })







        return binding.root




    }






    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




    }
    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }
    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ActivityMeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ActivityMeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}