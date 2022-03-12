package com.example.capstonandroid.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.FragmentTargetMeBinding
import com.example.capstonandroid.network.BackendApi
import com.example.capstonandroid.network.RetrofitClient
import retrofit2.Retrofit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private  lateinit var  retrofit: Retrofit  //레트로핏
private  lateinit var supplementService: BackendApi // api
/**
 * A simple [Fragment] subclass.
 * Use the [PersonalMeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PersonalMeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private var _binding: FragmentTargetMeBinding? = null
    private val binding: FragmentTargetMeBinding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        _binding = FragmentTargetMeBinding.inflate(layoutInflater)
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
        return inflater.inflate(R.layout.fragment_target_me, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.meTopNav.setOnItemSelectedListener {
//            println(it.itemId)
//            when (it.itemId) {
//                R.id.mefragment_profile -> {
//                    childFragmentManager.beginTransaction().replace(R.id.mefragment_container, ProfileMeFragment()).commit()
//                    //상단 액션바 변경
//                }
//            }
//            true
//        }

        // 처음 들어왔을때는 homeFragment
//        binding.meTopNav.selectedItemId = R.id.mefragment_profile

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
         * @return A new instance of fragment PersonalMeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PersonalMeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }



    private fun initRetrofit(){
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java);
    }
}