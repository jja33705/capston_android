package com.example.capstonandroid.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.capstonandroid.fragment.RidingFragment
import com.example.capstonandroid.fragment.RunningFragment

private const val NUM_TABS = 2

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    // 탭 개수 리턴
    override fun getItemCount(): Int {
        return NUM_TABS
    }

    // 포지션에 따라 프래그먼트 리턴
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RunningFragment()
            1 -> RidingFragment()
            else -> RunningFragment()
        }
    }

}