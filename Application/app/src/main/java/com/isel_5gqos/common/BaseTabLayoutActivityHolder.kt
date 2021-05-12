package com.isel_5gqos.common

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout

abstract class BaseTabLayoutActivityHolder: AppCompatActivity(), TabLayout.OnTabSelectedListener {

    protected var tabLayout : TabLayout? = null

    fun setupTabsTitle(tab: TabLayout.Tab?){
        tabLayout!!.getTabAt(0)!!.text = "YaYa"
        tabLayout!!.getTabAt(1)!!.text = "YaYa"
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {}

    override fun onTabUnselected(tab: TabLayout.Tab?) {}

    override fun onTabReselected(tab: TabLayout.Tab?) {}
}