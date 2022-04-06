package com.example.capstonandroid.fragment

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.R
import com.example.capstonandroid.activity.SNSDetailsActivity
import com.example.capstonandroid.adapter.RecyclerUserAdapter
import com.example.capstonandroid.databinding.FragmentHomeBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.SNSResponse
import com.example.capstonandroid.network.dto.UserData
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


object user {}


class MyActivity : AppCompatActivity() {
}
class HomeFragment : Fragment()  {

    private var param1: String? = null
    private var param2: String? = null

    private var mBinding: FragmentHomeBinding? = null
    private val binding get() = mBinding!!


    private var page = 0      // 현재 페이지
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
//      함수 초기화
        initRetrofit()

        page = 1       // 현재 페이지
        val sharedPreference = requireActivity().getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var token = "Bearer " + sharedPreference.getString("TOKEN","")
        println("홈 프레그먼트"+token)


        val list = ArrayList<UserData>()
        val adapter = RecyclerUserAdapter(list, { data -> adapterOnClick(data) })

        supplementService.SNSIndex(token, page).enqueue(object : Callback<SNSResponse>{
            override fun onResponse(
                call: Call<SNSResponse>,
                response: Response<SNSResponse>
            ) {
                println(response.body())

                if(response.isSuccessful) {

                    println(response.javaClass.name)
                    println(response.body()!!.data.size)
                    for (i in 0..response.body()!!.data.size-1) {
                        list.add(
                            UserData(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.sakai
                                )!!,
                                response.body()!!.data[i].title,
                                response.body()!!.data[i].user.name,
                                i,
                                response.body()!!.data[i].created_at,
                                response.body()!!.data[i].time,
                                response.body()!!.current_page
                            )
                        )
                    }
                    binding.lstUser.adapter = adapter
                    binding.lstUser.addItemDecoration(DistanceItemDecorator(10))

                    page ++
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


        binding.lstUser.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!binding.lstUser.canScrollVertically(1)){

                    supplementService.SNSIndex(token, page).enqueue(object : Callback<SNSResponse>{
                        override fun onResponse(
                            call: Call<SNSResponse>,
                            response: Response<SNSResponse>
                        ) {

                            if(response.isSuccessful&&response.body()!!.data.size!==0) {
                                for (i in 0..response.body()!!.data.size - 1) {
                                    list.add(
                                        UserData(
                                            ContextCompat.getDrawable(
                                                requireContext(),
                                                R.drawable.sakai
                                            )!!,
                                            response.body()!!.data[i].title,
                                            response.body()!!.data[i].user.name,
                                            i,
                                            response.body()!!.data[i].created_at,
                                            response.body()!!.data[i].time,
                                            response.body()!!.current_page
                                        )
                                    )
                                }

                                binding.lstUser.adapter!!.notifyItemInserted(10)

                                    page ++
                            }else{

                            }
                        }

                        override fun onFailure(call: Call<SNSResponse>, t: Throwable) {
                            TODO("Not yet implemented")
                        }
                    })

                }
            }
        })
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun adapterOnClick(data: UserData) {
        Toast.makeText(requireContext(), "FunCall Clicked -> ID : ${data.title}, Name : ${data.name}", Toast.LENGTH_SHORT).show()
        println(data.data_num)


        val nextIntent = Intent(requireContext(), SNSDetailsActivity::class.java)
        nextIntent.putExtra("data_num", data.data_num)
        nextIntent.putExtra("data_page", page)
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
        mBinding = null
        super.onDestroy()
    }

        companion object {
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
