package com.isel_5gqos.activities

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.os.Bundle
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.isel_5gqos.QosApp
import com.isel_5gqos.R
import com.isel_5gqos.activities.adapters.DashboardActivitViewPagerAdapter
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.factories.QosFactory
import com.isel_5gqos.factories.TestFactory
import com.isel_5gqos.jobs.WorkTypesEnum
import com.isel_5gqos.jobs.scheduleJob
import com.isel_5gqos.models.InternetViewModel
import com.isel_5gqos.models.QosViewModel
import com.isel_5gqos.models.TestViewModel
import com.isel_5gqos.utils.android_utils.AndroidUtils
import com.isel_5gqos.utils.publisher_subscriber.MessageEvent
import com.isel_5gqos.utils.publisher_subscriber.SessionMessageEvent
import com.isel_5gqos.utils.publisher_subscriber.SessionMessageTypeEnum
import com.isel_5gqos.utils.publisher_subscriber.StringMessageEvent
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


open class DashboardActivity : BaseTabLayoutActivityHolder() {

    private val model by lazy {
        ViewModelProvider(this)[InternetViewModel::class.java]
    }

    private lateinit var testFactory: TestFactory
    private val testModel by lazy {
        ViewModelProvider(this, testFactory)[TestViewModel::class.java]
    }

    private lateinit var qosFactory: QosFactory
    private val qosModel by lazy {
        ViewModelProvider(this, qosFactory)[QosViewModel::class.java]
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

    //<editor-fold desc="EVENTS">

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard2)
        val username = intent.getStringExtra(USER)?.toString() ?: ""
        val token = intent.getStringExtra(TOKEN)?.toString() ?: ""

        testFactory = TestFactory(savedInstanceState, username)
        qosFactory = QosFactory(savedInstanceState)
        dashboardActivitViewPager.adapter = dashboardActivitViewPagerAdapter

        tabLayout = dashboardActivityTabLayout.apply {
            setupWithViewPager(dashboardActivitViewPager)
            addOnTabSelectedListener(this@DashboardActivity)
            this.getTabAt(0)?.apply {
                this.view.background = resources.getDrawable(R.drawable.blue_500_background)
            }
        }

        startSession(SessionMessageTypeEnum.STOP_SESSION)
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSessionStateEvent(messageEvent: MessageEvent) {
        if (messageEvent !is SessionMessageEvent) return

        when (messageEvent.sessionState) {
            SessionMessageTypeEnum.START_SESSION -> {
                cancelAllJobs()
                startSession(SessionMessageTypeEnum.START_SESSION)
            }
            SessionMessageTypeEnum.STOP_SESSION -> {
                cancelAllJobs()
                testModel.getLastSession().observe(this) {
                    if (it?.endDate != 0L) return@observe
                    testModel.endSessionById(it.id)
                    startSession(SessionMessageTypeEnum.STOP_SESSION)
                    testModel.getLastSession().removeObservers(this)
                }

            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        if (testModel.liveData.value!!.id == DEFAULT_SESSION_ID) {
            cancelAllJobs()
            asyncTask(doInBackground = { testModel.deleteSessionInfo(DEFAULT_SESSION_ID){} })
        } else {
            AndroidUtils.notifyOnChannel(
                getString(R.string.active_session_noti),
                getString(R.string.active_session_noti_text),
                DashboardActivity::class.java,
                applicationContext
            )
        }
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        if (testModel.liveData.value!!.id != DEFAULT_SESSION_ID) {
            AndroidUtils.notifyOnChannel(
                getString(R.string.active_session_noti),
                getString(R.string.active_session_noti_text),
                DashboardActivity::class.java,
                applicationContext
            )
        }
        super.onStop()
    }

    //</editor-fold>

    //<editor-fold name="AUX FUNCTIONS">
    private fun cancelAllJobs() {

        jobs.forEach {
            val systemService = QosApp.msWebApi.ctx.getSystemService(JobScheduler::class.java)

            it.extras.get("args")
            systemService
                .cancel(it.id)
        }

        jobs.clear()
    }

    private fun startSession(sessionTypeState: SessionMessageTypeEnum) {

        when (sessionTypeState) {
            SessionMessageTypeEnum.START_SESSION -> startControlledSession(testModel.userName)
            SessionMessageTypeEnum.STOP_SESSION -> startDefaultSession(testModel.userName)
        }
    }

    private fun startControlledSession(username: String) {
        testModel.startSession(username)

        testModel.observe(this) {
            if (it.id == DEFAULT_SESSION_ID) return@observe
            EventBus.getDefault().post(StringMessageEvent(it.id))
            jobs.add(
                scheduleJob(
                    sessionId = it.id,
                    jobTypes = arrayListOf(WorkTypesEnum.RADIO_PARAMS_TYPES.workType, WorkTypesEnum.THROUGHPUT_TYPE.workType)
                )
            )
            testModel.liveData.removeObservers(this)
        }
    }

    private fun startDefaultSession(username: String) {
        asyncTask({ testModel.startDefaultSession(username) }) {
            EventBus.getDefault().post(StringMessageEvent(DEFAULT_SESSION_ID))
            jobs.add(
                scheduleJob(
                    sessionId = DEFAULT_SESSION_ID,
                    jobTypes = arrayListOf(WorkTypesEnum.RADIO_PARAMS_TYPES.workType, WorkTypesEnum.THROUGHPUT_TYPE.workType)
                )
            )
        }
    }

    //</editor-fold>

}