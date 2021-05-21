package com.isel_5gqos.activities

import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.tabs.TabLayout
import com.isel_5gqos.QosApp
import com.isel_5gqos.R
import com.isel_5gqos.activities.adapters.DashboardActivitViewPagerAdapter
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.jobs.WorkTypesEnum
import com.isel_5gqos.jobs.scheduleJob
import com.isel_5gqos.models.InternetViewModel
import com.isel_5gqos.models.TestViewModel
import com.isel_5gqos.utils.android_utils.AndroidUtils


open class DashboardActivity : BaseTabLayoutActivityHolder() {

    private val model by lazy {
        ViewModelProviders.of(this)[InternetViewModel::class.java]
    }

    private val testModel by lazy {
        ViewModelProviders.of(this)[TestViewModel::class.java]
    }

    private val dashboardActivitViewPagerAdapter by lazy {
        DashboardActivitViewPagerAdapter(supportFragmentManager)
    }
    private val dashboardActivitViewPager by lazy<ViewPager> {
        findViewById(R.id.dashboard_activity_viewPager)
    }
    private val dashboardActivityTabLayout by lazy<TabLayout> {
        findViewById(R.id.dashboard_tabs)
    }

    private val jobs = mutableListOf<JobInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard2)

        dashboardActivitViewPager.adapter = dashboardActivitViewPagerAdapter

        tabLayout = dashboardActivityTabLayout.apply {
            setupWithViewPager(dashboardActivitViewPager)
            addOnTabSelectedListener(this@DashboardActivity)
            this.getTabAt(0)?.apply {
                this.view.background = resources.getDrawable(R.drawable.blue_500_background)
            }
        }

        val username = intent.getStringExtra(USER)?.toString() ?: ""
        startDefaultSession(username)
    }

    private fun cancelAllJobs() {

        jobs.forEach {
            val systemService = QosApp.msWebApi.ctx.getSystemService(JobScheduler::class.java)

            it.extras.get("args")
            systemService
                .cancel(it.id)
        }

        jobs.clear()
    }

    private fun startDefaultSession(username: String) {
        asyncTask({ testModel.startDefaultSession(username) }) {
            jobs.add(
                scheduleJob(
                    sessionId = DEFAULT_SESSION_ID,
                    saveToDb = false,
                    jobTypes = arrayListOf(WorkTypesEnum.RADIO_PARAMS_TYPES.workType, WorkTypesEnum.THROUGHPUT_TYPE.workType)
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if(testModel.liveData.value!!.id == DEFAULT_SESSION_ID){
            cancelAllJobs()
        }
        else{
            AndroidUtils.notifyOnChannel(
                getString(R.string.active_session_noti),
                getString(R.string.active_session_noti_text),
                DashboardActivity::class.java,
                applicationContext
            )
        }
    }


}