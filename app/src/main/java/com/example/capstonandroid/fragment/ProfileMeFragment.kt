package com.example.capstonandroid.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.capstonandroid.R
import com.example.capstonandroid.activity.LoginActivity
import com.example.capstonandroid.activity.RegisterActivity
import com.example.capstonandroid.databinding.FragmentProfileMeBinding
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.dto.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import kotlin.math.log


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private  lateinit var  retrofit: Retrofit  //레트로핏
private  lateinit var supplementService: BackendApi // api

class ProfileMeFragment : Fragment(){
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

// 바인딩 객체 타입에 ?를 붙여서 null을 허용 해줘야한다. ( onDestroy 될 때 완벽하게 제거를 하기위해 )
 private var mBinding: FragmentProfileMeBinding? = null
// 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
 private val binding get() = mBinding!!




//    private val binding: FragmentProfileMeBinding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentProfileMeBinding.inflate(inflater, container, false)


        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }


        var sData = resources.getStringArray(R.array.my_array)
        var adapter =
            activity?.let { ArrayAdapter<String>(it,android.R.layout.simple_spinner_item,sData) }
        binding.spProfileMeSpinner.adapter = adapter

        binding.spProfileMeSpinner.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent : AdapterView<*>?, view: View?, position: Int, Int: Long) {
                if (position == 0) {
                    binding.tvProfileMeSpinner.setText("자전거를 선택하셨습니다")
                } else {
                    binding.tvProfileMeSpinner.setText("달리기를 선택하셨습니다")
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }



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


        supplementService.userGet(TOKEN.toString()).enqueue(object : Callback<LoginUserResponse>{
            override fun onResponse(
                call: Call<LoginUserResponse>,
                response: Response<LoginUserResponse>
            ) {
                println(response.body())
                var loginuserResponse: LoginUserResponse? = response.body()
                var user_name = loginuserResponse!!.name
                    var user_followers = loginuserResponse!!.followers.count().toString()
                    var user_followings = loginuserResponse!!.followings.count().toString()
                    var user_mmr = loginuserResponse!!.mmr

                binding.tvProfileMeName.setText(user_name!!)
                    binding.tvProfileMeFollowers.setText("팔로워 : " + user_followers)
                    binding.tvProfileMeFollowings.setText("팔로윙 : "+user_followings)
                    binding.tvProfileMeMmr.setText("MMR : " + user_mmr)

                if (loginuserResponse.profile.equals(null)||loginuserResponse.profile.equals("img")){
                        binding.tvProfileMePicture.setImageResource(R.drawable.main_profile)
                    }else {
                        val url = "https://cdn.pixabay.com/photo/2021/08/03/07/03/orange-6518675_960_720.jpg"
                        Glide.with(this@ProfileMeFragment).load(url).circleCrop().into(binding.tvProfileMePicture)

                    }
            }

            override fun onFailure(call: Call<LoginUserResponse>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })


//        var email = "test@gmail.com"
//        var password = "1234"
//        val login = Login(
//            email = email,
//            password = password
//        )

//        supplementService.userGet(TOKEN.toString()).enqueue(object : Callback<LoginUserResponse>{
//            override fun onResponse(
//                call: Call<LoginUserResponse>,
//                response: Response<LoginUserResponse>
//            ) {
//                if(response.isSuccessful){
//                    println("다음 페이지 넘기기")
////                  콜백 응답으로 온것
//                    println(response.body())
//
//                    var loginuserResponse: LoginUserResponse? = response.body()
//
//                        데이터 클래스 USER 사용방법
//                        var user: User? = loginResponse!!.user
//                        print(user!!.birth)
//                    println(loginuserResponse!!.name)
//                    user_name = loginuserResponse!!.name
//                    user_followers = loginuserResponse!!.followers.count().toString()
//                    user_followings = loginuserResponse!!.followings.count().toString()
//                    user_mmr = loginuserResponse!!.mmr
//
//                    println("유저 이름 :"+ user_name)
//
//                    println("팔로워 수 "+user_followers)
//                    println("팔로윙 수 "+user_followings)
//                    println("MMR"+user_mmr)
//
//                    binding.tvProfileMeName.setText(user_name!!)
//                    binding.tvProfileMeFollowers.setText("팔로워 : " + user_followers)
//                    binding.tvProfileMeFollowings.setText("팔로윙 : "+user_followings)
//                    binding.tvProfileMeMmr.setText("MMR : " + user_mmr)
//
//
//                    println(user!!.profile)
//                    if (user!!.profile.equals(null)){
//                        binding.tvProfileMePicture.setImageResource(R.drawable.main_profile)
//                    }else {
//                        val url = "https://cdn.pixabay.com/photo/2021/08/03/07/03/orange-6518675_960_720.jpg"
//                        Glide.with(this@ProfileMeFragment).load(url).circleCrop().into(binding.tvProfileMePicture)
//
//                    }
//
//                }else {
//                    println("갔지만 실패")
//                    println(response.body())
//                    println(response.message())
//                    println(response.code())
//                }
//            }
//
//            override fun onFailure(call: Call<LoginUserResponse>, t: Throwable) {
//            }
//
//        })

//        supplementService.loginPost(login).enqueue(object : Callback<LoginResponse> {
//            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
//
//                if(response.isSuccessful){
//                    println("성공 프로필 프래그먼트")
////                  콜백 응답으로 온것
//                    println(response.body())
////                    데이터 클래스 USER 사용방법
//
//                    var loginResponse: LoginResponse? = response.body()
//                    var user: User? = loginResponse!!.user
////                        데이터 클래스 USER 사용방법
////                        var user: User? = loginResponse!!.user
////                        print(user!!.birth)
//                    println(user!!.name)
//                    user_name = user!!.name
//                    user_followers = user!!.followers.count().toString()
//                    user_followings = user!!.followings.count().toString()
//                    user_mmr = user!!.mmr
//
//                    println("유저 이름 :"+ user_name)
//
//                    println("팔로워 수 "+user_followers)
//                    println("팔로윙 수 "+user_followings)
//                    println("MMR"+user_mmr)
//
//                    binding.tvProfileMeName.setText(user_name!!)
//                    binding.tvProfileMeFollowers.setText("팔로워 : " + user_followers)
//                    binding.tvProfileMeFollowings.setText("팔로윙 : "+user_followings)
//                    binding.tvProfileMeMmr.setText("MMR : " + user_mmr)
//
//
//                    println(user!!.profile)
//                    if (user!!.profile.equals(null)){
//                        binding.tvProfileMePicture.setImageResource(R.drawable.main_profile)
//                    }else {
//                        val url = "https://cdn.pixabay.com/photo/2021/08/03/07/03/orange-6518675_960_720.jpg"
//                        Glide.with(this@ProfileMeFragment).load(url).circleCrop().into(binding.tvProfileMePicture)
//
//                    }
//
//                     }else {
//                    println("갔지만 실패")
//                    println(response.body())
//                    println(response.message())
//                    println(response.code())
//                }
//            }
//
//
//            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
//
//                println("실패")
//                println(t.message)
//            }
//
//        })
        val loginActivity = Intent(requireContext(), LoginActivity::class.java)
        //
//      사용자이름 바꾸기
        binding.logout.setOnClickListener {
            supplementService.logOut(TOKEN.toString()).enqueue(object : Callback<LogoutResponse> {
                override fun onResponse(
                    call: Call<LogoutResponse>,
                    response: Response<LogoutResponse>
                ) {
                    if (response.isSuccessful) {
                        println("로그아웃이 성공되었습니다! 성공 ")
//                  콜백 응답으로 온것
                        println(response.body())
                        startActivity(loginActivity)

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
        return binding.root

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)





    }





    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }

    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileMeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileMeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
