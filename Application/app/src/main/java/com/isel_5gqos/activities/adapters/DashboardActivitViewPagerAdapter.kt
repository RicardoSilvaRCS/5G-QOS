package com.isel_5gqos.activities.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.isel_5gqos.activities.fragments.*

class DashboardActivitViewPagerAdapter(fragmentManager: FragmentManager):FragmentPagerAdapter(fragmentManager) {
    private val itemMap = mapOf(
        0 to Pair(FragmentTable(), "Serving Cell"),
        1 to Pair(FragmentChartSession(),"Main Session"),
        2 to Pair(FragmentControlledSession(),"Controlled Session"),
        3 to Pair(FragmentTestPlans(),"Test Plans"),
        4 to Pair(FragmentInfo(),"Info")
    )

    override fun getCount():Int = itemMap.size
    override fun getItem(position: Int): Fragment = itemMap[position]!!.first
    override fun getPageTitle(position: Int): CharSequence? = itemMap[position]?.second
}