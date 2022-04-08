package com.example.capstonandroid.fragment

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.R
import com.example.capstonandroid.activity.MeDetailsActivity
import com.example.capstonandroid.activity.SNSDetailsActivity
import com.example.capstonandroid.adapter.RecyclerUserAdapter
import com.example.capstonandroid.databinding.FragmentActivityMeBinding
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.dto.*
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


    private var page = 1       // 현재 페이지

    private var pageNum = 1;
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


//        함수 초기화
        initRetrofit()
////      토큰 불러오기

       page = 1       // 현재 페이지
        val sharedPreference = requireActivity().getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var token = "Bearer " + sharedPreference.getString("TOKEN", "")
        println("프로필 미 프래그먼트 + " + token)

        val list2 = ArrayList<UserData>()
        val adapter2 = RecyclerUserAdapter(list2, { data -> adapterOnClick(data) })

        supplementService.myIndex(token, page).enqueue(object : Callback<MySNSResponse> {
            override fun onResponse(call: Call<MySNSResponse>, response: Response<MySNSResponse>) {
                println(response.body())
                if (response.isSuccessful) {
                    println(response.body())


                    println(response.body()!!.data.size.toString()+"ㅇㅁㄴㅇㄴㅁㅇㄴㅁㅇㅁㄴㅇㅁㄴㅇㄴㅁㅇ")

                    for (i in 0..response.body()!!.data.size -1) {
                        list2.add(
                            UserData(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.sakai
                                )!!,
                                response.body()!!.data[i].title,
                                response.body()!!.data[i].kind,
                                i,
                                response.body()!!.data[i].created_at,
                                response.body()!!.data[i].time,
                            )
                        )
                    }
                    binding.lstUser2.adapter = adapter2
                    binding.lstUser2.addItemDecoration(DistanceItemDecorator(10))

                    page++
                } else {

                }
//
            }

            override fun onFailure(call: Call<MySNSResponse>, t: Throwable) {

            }

        })

        binding.lstUser2.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!binding.lstUser2.canScrollVertically(1)){

                    supplementService.myIndex(token, page).enqueue(object : Callback<MySNSResponse>{
                        override fun onResponse(
                            call: Call<MySNSResponse>,
                            response: Response<MySNSResponse>
                        ) {

                            if(response.isSuccessful&&response.body()!!.data.size!==0) {
                                for (i in 0..response.body()!!.data.size - 1) {
                                    list2.add(
                                        UserData(
                                            ContextCompat.getDrawable(
                                                requireContext(),
                                                R.drawable.sakai
                                            )!!,
                                            response.body()!!.data[i].title,
                                            response.body()!!.data[i].kind,
                                            i,
                                            response.body()!!.data[i].created_at,
                                            response.body()!!.data[i].time,
                                        )
                                    )
                                }

                                binding.lstUser2.adapter!!.notifyItemInserted(10)
                                page++

                            }else{

                            }
                        }

                        override fun onFailure(call: Call<MySNSResponse>, t: Throwable) {
                            TODO("Not yet implemented")
                        }
                    })

                }
            }
        })
        return binding.root
    }

    private fun adapterOnClick(data: UserData) {

        Toast.makeText(requireContext(), "FunCall Clicked -> ID : ${data.title}, Name : ${data.name}", Toast.LENGTH_SHORT).show()
        println(data.data_num)


        val nextIntent = Intent(requireContext(), MeDetailsActivity::class.java)
        nextIntent.putExtra("data_num", data.data_num)
        nextIntent.putExtra("data_page", page-1)
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
