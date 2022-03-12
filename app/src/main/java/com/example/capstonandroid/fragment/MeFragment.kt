package com.example.capstonandroid.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.capstonandroid.databinding.FragmentMeBinding
import androidx.appcompat.app.AppCompatActivity
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.FragmentActivityMeBinding
import com.example.capstonandroid.databinding.FragmentProfileMeBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
<<<<<<< HEAD
 * Use the [MeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentMeBinding? = null
    private val binding: FragmentMeBinding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = FragmentMeBinding.inflate(layoutInflater)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//       여기 중요함 . !
        // Inflate the layout for this fragment
        _binding = FragmentMeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.meTopNav.setOnItemSelectedListener {
            println("버튼 눌림")
            println(it.itemId)
            when (it.itemId) {
                R.id.mefragment_profile -> {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.mefragment_container, ProfileMeFragment()).commit()

                }
                R.id.mefragment_target -> {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.mefragment_container, PersonalMeFragment()).commit()

                }
                R.id.mefragment_activity -> {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.mefragment_container, ActivityMeFragment()).commit()
                }
            }
            true
        }

//         처음 들어왔을때는 homeFragment
        binding.meTopNav.selectedItemId = R.id.mefragment_profile

    }
    // 프래그먼트가 삭제 될 시

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