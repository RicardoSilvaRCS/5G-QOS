package com.isel_5gqos.activities

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.isel_5gqos.QosApp
import com.isel_5gqos.R
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.dtos.RadioParametersDto
import com.isel_5gqos.dtos.ThroughPutDto
import com.isel_5gqos.jobs.scheduleRadioParametersJob
import com.isel_5gqos.models.InternetViewModel
import com.isel_5gqos.models.TestViewModel
import kotlin.math.max
import kotlin.math.min


class DashboardActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener, OnChartValueSelectedListener {

    private val model by lazy {
        ViewModelProviders.of(this)[InternetViewModel::class.java]
    }

    private val testModel by lazy {
        ViewModelProviders.of(this)[TestViewModel::class.java]
    }

    private val jobs = mutableListOf<JobInfo>()

    /**INIT UI ELEMENTS**/
    private val chart: LineChart by lazy {
        findViewById(R.id.chart)
    }

    private val servingCellChart: LineChart by lazy {
        findViewById(R.id.servingCellChart)
    }

    private lateinit var seekBarX: SeekBar
    private lateinit var seekBarY: SeekBar
    private lateinit var tvX: TextView
    private lateinit var tvY: TextView

    /**END**/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard2)

        val username = intent.getStringExtra(USER)?.toString() ?: ""

        /**Create new Session*/

        /*val createButton = findViewById<Button>(R.id.createSession)
        val deleteButton = findViewById<Button>(R.id.endSession)

        createButton.setOnClickListener {
            cancelAllJobs()

            testModel.startSession(username)
            testModel.observe(this) {
                //jobs.add(scheduleThroughPutJob(sessionId = testModel.value.id))
                jobs.add(scheduleRadioParametersJob(sessionId = testModel.value.id, true))

                testModel.registerRadioParametersChanges(testModel.value.id).observe(this) {
                    val radioParameters = RadioParametersDto.convertRadioParametersToDto(it)
                }

                Log.v(TAG, "Started session")
            }

        }

        deleteButton.setOnClickListener {
            cancelAllJobs()

//            jobs.add(scheduleRadioParametersJob(DEFAULT_SESSION_ID, false))
            jobs.add(scheduleThroughPutJob(DEFAULT_SESSION_ID))
        }*/

        /**Start real Time Session*/
        startDefaultSession(username)

        initLineChart(chart,initThroughputDataLine())
        testModel.registerThroughPutChanges(DEFAULT_SESSION_ID).observe(this) {
            if(it == null) return@observe
            val data = chart.data ?: return@observe

            val throughPut = ThroughPutDto.convertThroughPutToDto(it)

            val rxdataSet = data.dataSets[ThroughputIndex.RX]
            val txDataSet = data.dataSets[ThroughputIndex.TX]
            data.addEntry(Entry(rxdataSet.entryCount.toFloat(), throughPut.rxResult.toFloat()), ThroughputIndex.RX)
            data.addEntry(Entry(txDataSet.entryCount.toFloat(), throughPut.txResult.toFloat()), ThroughputIndex.TX)

            if(chart.axisLeft.axisMaximum < throughPut.rxResult || chart.axisLeft.axisMaximum < throughPut.txResult)
                chart.axisLeft.axisMaximum = max(throughPut.rxResult,throughPut.txResult).toFloat()

            // enable touch gestures
            chart.setTouchEnabled(true)

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(10f)

            // move to the latest entry
//            chart.moveViewToX(chart.data.entryCount.toFloat())

            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        }

        initLineChart(servingCellChart,lineInitData = initServingCellData(),isNegative = true)
        testModel.registerRadioParametersChanges(DEFAULT_SESSION_ID).observe(this) {
            if(it == null || it.isEmpty()) return@observe
            val data = servingCellChart.data ?: return@observe

            val radioParametersDto = RadioParametersDto.convertRadioParametersToDto(it)

            val rssiDataSet = data.dataSets[ServingCellIndex.RSSI]
            val rsrpDataSet = data.dataSets[ServingCellIndex.RSRP]
            val rsqrDataSet = data.dataSets[ServingCellIndex.RSQR]
            val rssnrDataSet = data.dataSets[ServingCellIndex.RSSNR]

            val servingCell = radioParametersDto.find { current ->  current.isServingCell || current.no == 1 }

            data.addEntry(Entry(rssiDataSet.entryCount.toFloat(), servingCell!!.rssi!!.toFloat()), ServingCellIndex.RSSI)
            data.addEntry(Entry(rsrpDataSet.entryCount.toFloat(), servingCell.rsrp!!.toFloat()), ServingCellIndex.RSRP)
            data.addEntry(Entry(rsqrDataSet.entryCount.toFloat(), servingCell.rsrq!!.toFloat()), ServingCellIndex.RSQR)
            data.addEntry(Entry(rssnrDataSet.entryCount.toFloat(), servingCell.rssnr!!.toFloat()), ServingCellIndex.RSSNR)

            val minimumValue = min(min(min(servingCell.rssi!!,servingCell.rsrp),servingCell.rsrq),servingCell.rssnr)
            val maximumValue = max(max(max(servingCell.rssi!!,servingCell.rsrp),servingCell.rsrq),servingCell.rssnr)
            if(servingCellChart.axisLeft.axisMaximum < maximumValue)
                servingCellChart.axisLeft.axisMaximum = maximumValue.toFloat()
            if(servingCellChart.axisLeft.axisMinimum > minimumValue)
                servingCellChart.axisLeft.axisMaximum = minimumValue.toFloat()

            // enable touch gestures
            servingCellChart.setTouchEnabled(true)


            // limit the number of visible entries
            servingCellChart.setVisibleXRangeMaximum(10f)

            // move to the latest entry
//            servingCellChart.moveViewToX(chart.data.entryCount.toFloat())

            servingCellChart.data.notifyDataChanged()
            servingCellChart.notifyDataSetChanged()
        }

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
            jobs.add(scheduleRadioParametersJob(sessionId = DEFAULT_SESSION_ID, saveToDb = false))
            //jobs.add(scheduleThroughPutJob(sessionId = DEFAULT_SESSION_ID))
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
        rssi.highLightColor = Color.rgb(244, 117, 117)
        rssi.setDrawValues(false)


        val rsrpValue = java.util.ArrayList<Entry>()
        rsrpValue.add(Entry(0f, 0f))
        val rsrp = LineDataSet(rsrpValue, "RSRP")
        rsrp.lineWidth = 2.5f
        rsrp.circleRadius = 1f
        rsrp.highLightColor = Color.rgb(244, 117, 117)
        rsrp.color = ColorTemplate.VORDIPLOM_COLORS[0]
        rsrp.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0])
        rsrp.setDrawValues(false)

        val rsqrValue = java.util.ArrayList<Entry>()
        rsqrValue.add(Entry(0f, 0f))
        val rsqr = LineDataSet(rsqrValue, "RSQR")
        rsqr.lineWidth = 2.5f
        rsqr.circleRadius = 1f
        rsqr.highLightColor = Color.rgb(244, 117, 117)
        rsqr.color = ColorTemplate.VORDIPLOM_COLORS[0]
        rsqr.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0])
        rsqr.setDrawValues(false)


        val rssnrValue = java.util.ArrayList<Entry>()
        rssnrValue.add(Entry(0f, 0f))
        val rssnr = LineDataSet(rssnrValue, "RSSNR")
        rssnr.lineWidth = 2.5f
        rssnr.circleRadius = 1f
        rssnr.highLightColor = Color.rgb(244, 117, 117)
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
    private fun initLineChart(lineChart: LineChart, lineInitData: LineData,isNegative:Boolean = false) {

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

        val yAxis = lineChart.axisLeft
        yAxis.setDrawGridLines(true)
        yAxis.axisMaximum = if(isNegative) 0f else 10f
        yAxis.axisMinimum = if(isNegative) -10f else 0f

        lineChart.axisRight.isEnabled = false

        lineChart.data = lineInitData
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        tvX.text = seekBarX.progress.toString()
        tvY.text = seekBarY.progress.toString()

        chart.invalidate();
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        Log.i(TAG, e.toString());
        Log.i(TAG, "low: " + chart.lowestVisibleX + ", high: " + chart.highestVisibleX);
        Log.i(TAG, "xMin: " + chart.xChartMin + ", xMax: " + chart.xChartMax + ", yMin: " + chart.yChartMin + ", yMax: " + chart.yChartMax);
    }

    override fun onNothingSelected() {}

    /**END**/


}