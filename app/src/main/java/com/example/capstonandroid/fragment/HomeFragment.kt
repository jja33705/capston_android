package com.example.capstonandroid.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.capstonandroid.R
import com.example.capstonandroid.activity.MainActivity
import com.example.capstonandroid.databinding.FragmentHomeBinding
import com.example.capstonandroid.network.dto.Login
import com.example.capstonandroid.network.dto.LogoutResponse
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)


            initRetrofit()




//          edittext 이메일 값 받아 오기
            var email = "test@gmail.com"
//            println(email)

//          edittext 비밀번호 값 받아오기
            var password = "1234"

            val login = Login(
                email = email.toString(),
                password = password.toString()
            )


            val bindinghome = view?.let { it1 -> FragmentHomeBinding.bind(it1) }


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

        val bindinghome = FragmentHomeBinding.bind(view)
        bindinghome.fragmentHomeA.setText("")

        val activity = activity as MainActivity?

//      함수 초기화
        initRetrofit()

        val sharedPreference = requireActivity().getSharedPreferences("other", 0)

//      이 타입이 디폴트 값
        var TOKEN = "Bearer " + sharedPreference.getString("TOKEN","")
        println(TOKEN)







        bindinghome.logout.setOnClickListener {
            supplementService.logOut(TOKEN.toString()).enqueue(object : Callback<LogoutResponse> {
                override fun onResponse(
                    call: Call<LogoutResponse>,
                    response: Response<LogoutResponse>
                ) {
                    if (response.isSuccessful) {
                        println("로그아웃이 성공되었습니다! 성공 ")
//                  콜백 응답으로 온것
                        println(response.body())





                    } else {
                        println("갔지만 실패")
                        println(response.body())
                        println(response.message())
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                    TODO("Not yet implemented")
                }
            })


            }
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