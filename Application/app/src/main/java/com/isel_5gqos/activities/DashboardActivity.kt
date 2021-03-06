package com.isel_5gqos.activities

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.Intent
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
import com.isel_5gqos.jobs.JobTypeEnum
import com.isel_5gqos.jobs.scheduleJob
import com.isel_5gqos.models.InternetViewModel
import com.isel_5gqos.models.QosViewModel
import com.isel_5gqos.models.TestViewModel
import com.isel_5gqos.models.observeOnce
import com.isel_5gqos.common.utils.android_utils.AndroidUtils
import com.isel_5gqos.common.utils.publisher_subscriber.MessageEvent
import com.isel_5gqos.common.utils.publisher_subscriber.SessionMessageEvent
import com.isel_5gqos.common.utils.publisher_subscriber.SessionMessageTypeEnum
import com.isel_5gqos.common.utils.publisher_subscriber.StringMessageEvent
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.sql.Timestamp


open class DashboardActivity : BaseTabLayoutActivityHolder() {

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

        reportUnreportedTests()

        tabLayout = dashboardActivityTabLayout.apply {
            setupWithViewPager(dashboardActivitViewPager)
            addOnTabSelectedListener(this@DashboardActivity)
            this.getTabAt(0)?.apply {
                this.view.background = resources.getDrawable(R.drawable.blue_500_background)
            }
        }

        testModel.getLastSession().observeOnce(this){
            if(it != null) {
                testModel.updateModel(it)
                launchJobScheduler(it.id)
                EventBus.getDefault().post(StringMessageEvent(it.id))
            }else{
                startDefaultSession(testModel.userName)
            }
        }
    }

    private fun reportUnreportedTests(){
        qosModel.getFinishedTestPlans().observeOnce(this) {
            it.forEach { testPlan ->
                qosModel.observeOnce(this){ user ->
                    qosModel.getTestsByTestPlanId(testPlan.id).observeOnce(this){ testPlanResults ->
                        qosModel.reportTestResults(user.userToken,user.deviceId,testPlanResults.filter { testPlanResult -> !testPlanResult.isReported })
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSessionStateEvent(messageEvent: MessageEvent) {
        if (messageEvent !is SessionMessageEvent) return

        val endTime = Timestamp(System.currentTimeMillis())
        cancelAllJobs()

        when (messageEvent.sessionState) {
            SessionMessageTypeEnum.START_SESSION -> {
                testModel.endSessionById(DEFAULT_SESSION_ID,endTime)
                startControlledSession(testModel.userName)
            }
            SessionMessageTypeEnum.STOP_SESSION -> {
                testModel.getLastSession().observeOnce(this) {
                    if (it?.endDate != 0L) return@observeOnce
                    testModel.endSessionById(it.id,endTime)
                    startDefaultSession(testModel.userName)
                }
            }
            SessionMessageTypeEnum.LOGOUT_USER -> {
                testModel.getLastSession().observeOnce(this) {
                    if(it == null) testModel.endSessionById(DEFAULT_SESSION_ID,endTime)
                    else if (it.endDate == 0L) testModel.endSessionById(it.id,endTime) //Ending unfinished session

                    qosModel.getLoggedUser().observeOnce(this){ login ->
                        qosModel.logoutActiveUser(login.user, login.token){
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }

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

    override fun onStart() {
        EventBus.getDefault().register(this)
        super.onStart()
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
            systemService?.cancel(it.id)
        }

        jobs.clear()
    }

    private fun startControlledSession(username: String) {
        testModel.startSession(username){
            if (it.id == DEFAULT_SESSION_ID) return@startSession
            launchJobScheduler(it.id)
            EventBus.getDefault().post(StringMessageEvent(it.id))
        }
    }

    private fun launchJobScheduler(sessionId : String){
        jobs.add(
            scheduleJob(
                sessionId = sessionId,
                jobTypes = arrayListOf(JobTypeEnum.RADIO_PARAMS_TYPE.jobType, JobTypeEnum.THROUGHPUT_TYPE.jobType)
            )
        )
    }

    private fun startDefaultSession(username: String) {
        asyncTask({ testModel.startDefaultSession(username) }) {
            EventBus.getDefault().post(StringMessageEvent(DEFAULT_SESSION_ID))
           launchJobScheduler(DEFAULT_SESSION_ID)
        }
    }

    //</editor-fold>

}