package com.isel_5gqos.activities.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.isel_5gqos.activities.fragments.FragmentControlledSession
import com.isel_5gqos.activities.fragments.FragmentMainSession

class DashboardActivitViewPagerAdapter(fragmentManager: FragmentManager):FragmentPagerAdapter(fragmentManager) {
    private val itemMap = mapOf(
        0 to Pair(FragmentMainSession(),"Main Session"),
        1 to Pair(FragmentControlledSession(),"Controlled Session")
    )

    override fun getCount():Int = itemMap.size
    override fun getItem(position: Int): Fragment = itemMap[position]!!.first
    override fun getPageTitle(position: Int): CharSequence? = itemMap[position]!!.second
}