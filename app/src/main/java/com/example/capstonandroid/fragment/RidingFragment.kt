package com.example.capstonandroid.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.capstonandroid.R
import com.example.capstonandroid.activity.SelectTrackActivity
import com.example.capstonandroid.databinding.FragmentRidingBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RidingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RidingFragment : Fragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentRidingBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentRidingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 버튼 클릭 리스너 등록
        binding.ridingNormalMatch.setOnClickListener(this)
        binding.ridingFriendlyMatch.setOnClickListener(this)
        binding.ridingRankMatch.setOnClickListener(this)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RidingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RidingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(view: View?) {
        var matchType = ""

        // 매치 방식에 따라 분기처리
        when (view?.id) {
            R.id.riding_normal_match -> {
                println("normal")
                matchType = "normal"
            }
            R.id.riding_friendly_match -> {
                println("friendly")
                matchType = "friendly"
            }
            R.id.riding_rank_match -> {
                println("rank")
                matchType = "rank"
            }
        }


        val intent = Intent(activity, SelectTrackActivity::class.java)
        intent.putExtra("exerciseKind", "riding")
        intent.putExtra("matchType", matchType)
        startActivity(intent)
    }
}