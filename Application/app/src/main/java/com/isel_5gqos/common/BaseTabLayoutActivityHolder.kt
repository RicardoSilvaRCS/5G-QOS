package com.isel_5gqos.common

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.isel_5gqos.R

abstract class BaseTabLayoutActivityHolder: AppCompatActivity(), TabLayout.OnTabSelectedListener {

    protected var tabLayout : TabLayout? = null

    fun setupTabsTitle(tab: TabLayout.Tab?){
        tabLayout!!.getTabAt(0)!!.text = "YaYa"
        tabLayout!!.getTabAt(1)!!.text = "YaYa"
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        tab?.apply{
            this.view.background = resources.getDrawable(R.drawable.blue_500_background)
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        tab?.apply{
            this.view.background = resources.getDrawable(R.drawable.blue_300_background)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {}

}