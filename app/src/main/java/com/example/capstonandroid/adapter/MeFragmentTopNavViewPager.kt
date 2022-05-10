package com.example.capstonandroid.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.capstonandroid.fragment.ActivityMeFragment
import com.example.capstonandroid.fragment.GoalFragment
import com.example.capstonandroid.fragment.ProfileMeFragment

class MeFragmentTopNavViewPager(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle){

    companion object {
        private const val NUM_TABS = 3
    }
    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return ProfileMeFragment()
            1 -> return GoalFragment()
            2 -> return ActivityMeFragment()
        }
        return ProfileMeFragment()
    }
}