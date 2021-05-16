package com.isel_5gqos.activities

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.graphics.Color
import android.os.Bundle
import android.widget.*
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

    val jobs = mutableListOf<JobInfo>()

    /**INIT UI ELEMENTS**/
    private val chart: LineChart by lazy {
        findViewById(R.id.throughput_chart)
    }

    private val servingCellChart: LineChart by lazy {
        findViewById(R.id.serving_cell_chart)
    }

    private lateinit var seekBarX: SeekBar
    private lateinit var seekBarY: SeekBar
    private lateinit var tvX: TextView
    private lateinit var tvY: TextView

    /**END**/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard2)

        dashboardActivitViewPager.adapter = dashboardActivitViewPagerAdapter

        tabLayout = dashboardActivityTabLayout.apply {
            setupWithViewPager(dashboardActivitViewPager)
            addOnTabSelectedListener(this@DashboardActivity)
            this.getTabAt(0)?.apply {
                this.view.background = resources.getDrawable(R.drawable.purple_700_background)
            }
        }

//        val username = intent.getStringExtra(USER)?.toString() ?: ""

        /**Create new Session*/

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

    /**Initializing ThroughPut graphic info**/

    private fun initThroughputDataLine(): LineData {

        val rxValues = java.util.ArrayList<Entry>()
        val txValues = java.util.ArrayList<Entry>()

        rxValues.add(Entry(0f, 0f))
        txValues.add(Entry(0f, 0f))

        val rx = LineDataSet(rxValues, "RX Kbit/s")
        rx.lineWidth = 2.5f
        rx.circleRadius = 1f
        rx.highLightColor = Color.rgb(244, 117, 117)
        rx.setDrawValues(false)

        val tx = LineDataSet(txValues, "TX Kbit/s")
        tx.lineWidth = 2.5f
        tx.circleRadius = 1f
        tx.highLightColor = Color.rgb(244, 117, 117)
        tx.color = ColorTemplate.VORDIPLOM_COLORS[0]
        tx.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0])
        tx.setDrawValues(false)


        val sets = java.util.ArrayList<ILineDataSet>()
        sets.add(rx)
        sets.add(tx)

        return LineData(sets)
    }

    /**END**/

    /**Initializing UI ServingCell graphic**/

    private fun initServingCellData(): LineData {

        val rssiValue = java.util.ArrayList<Entry>()
        rssiValue.add(Entry(0f, 0f))
        val rssi = LineDataSet(rssiValue, "RSSI")
        rssi.lineWidth = 2.5f
        rssi.circleRadius = 1f
        rssi.highLightColor = Color.rgb(163, 145, 3)
        rssi.setDrawValues(false)


        val rsrpValue = java.util.ArrayList<Entry>()
        rsrpValue.add(Entry(0f, 0f))
        val rsrp = LineDataSet(rsrpValue, "RSRP")
        rsrp.lineWidth = 2.5f
        rsrp.circleRadius = 1f
        rsrp.highLightColor = Color.rgb(0, 255, 0)
        rsrp.color = ColorTemplate.VORDIPLOM_COLORS[1]
        rsrp.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[1])
        rsrp.setDrawValues(false)

        val rsqrValue = java.util.ArrayList<Entry>()
        rsqrValue.add(Entry(0f, 0f))
        val rsqr = LineDataSet(rsqrValue, "RSQR")
        rsqr.lineWidth = 2.5f
        rsqr.circleRadius = 1f
        rsqr.highLightColor = Color.rgb(0, 0, 255)
        rsqr.color = ColorTemplate.VORDIPLOM_COLORS[2]
        rsqr.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[2])
        rsqr.setDrawValues(false)


        val rssnrValue = java.util.ArrayList<Entry>()
        rssnrValue.add(Entry(0f, 0f))
        val rssnr = LineDataSet(rssnrValue, "RSSNR")
        rssnr.lineWidth = 2.5f
        rssnr.circleRadius = 1f
        rssnr.highLightColor = Color.rgb(255, 0, 0)
        rssnr.color = ColorTemplate.VORDIPLOM_COLORS[0]
        rssnr.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0])
        rssnr.setDrawValues(false)


        val sets = java.util.ArrayList<ILineDataSet>()
        sets.add(rssi)
        sets.add(rsrp)
        sets.add(rsqr)
        sets.add(rssnr)

        return LineData(sets)
    }

    /**END**/


    /**Initializing UI graphics**/
    private fun initLineChart(lineChart: LineChart, lineInitData: LineData, isNegative: Boolean = false, granularity: Float = 1f) {

        lineChart.setBackgroundColor(Color.WHITE)
        // disable description text
        lineChart.description.isEnabled = false

        // enable scaling and dragging
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setDrawGridBackground(false)

        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.setPinchZoom(true)

        val xAxis = lineChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.isEnabled = true
        xAxis.granularity = granularity

        val yAxis = lineChart.axisLeft
        yAxis.setDrawGridLines(true)
        yAxis.axisMaximum = if (isNegative) 0f else 10f
        yAxis.axisMinimum = if (isNegative) -10f else 0f

        lineChart.axisRight.isEnabled = false

        lineChart.data = lineInitData
    }
    /**END**/

}