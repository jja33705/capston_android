package com.example.capstonandroid.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.capstonandroid.R
import com.example.capstonandroid.activity.SelectTrackActivity
import com.example.capstonandroid.databinding.FragmentRunningBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RunningFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RunningFragment : Fragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentRunningBinding? = null
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
        _binding = FragmentRunningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.runningNormalMatch.setOnClickListener(this)
        binding.runningFriendlyMatch.setOnClickListener(this)
        binding.runningRankMatch.setOnClickListener(this)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RunningFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RunningFragment().apply {
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
            R.id.running_normal_match -> {
                println("normal")
                matchType = "혼자하기"
            }
            R.id.running_friendly_match -> {
                println("friendly")
                matchType = "친선전"
            }
            R.id.running_rank_match -> {
                println("rank")
                matchType = "랭크전"
            }
        }

        val intent = Intent(activity, SelectTrackActivity::class.java)
        intent.putExtra("exerciseKind", "R")
        intent.putExtra("matchType", matchType)
        startActivity(intent)
    }
}