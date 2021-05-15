package com.isel_5gqos.activities.fragments

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.isel_5gqos.QosApp
import com.isel_5gqos.R
import com.isel_5gqos.activities.USER
import com.isel_5gqos.common.DEFAULT_SESSION_ID
import com.isel_5gqos.common.ServingCellIndex
import com.isel_5gqos.common.ThroughputIndex
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.dtos.RadioParametersDto
import com.isel_5gqos.dtos.ThroughPutDto
import com.isel_5gqos.jobs.WorkTypesEnum
import com.isel_5gqos.jobs.scheduleJob
import com.isel_5gqos.models.InternetViewModel
import com.isel_5gqos.models.TestViewModel
import kotlinx.android.synthetic.main.fragment_main_session.*
import java.lang.Integer.max
import java.lang.Integer.min
import java.lang.Long.max

class FragmentMainSession : Fragment() {

    /**INIT UI ELEMENTS**/

    private val model by lazy {
        ViewModelProviders.of(this)[InternetViewModel::class.java]
    }
    private val jobs = mutableListOf<JobInfo>()
    private val testModel by lazy {
        ViewModelProviders.of(this)[TestViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_main_session, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val username = activity!!.intent.getStringExtra(USER)?.toString() ?: ""
        startDefaultSession(username)

        initLineChart(throughput_chart, initThroughputDataLine())

        initLineChart(g1,initThroughputDataLine())
        initLineChart(g2,initThroughputDataLine())
        initLineChart(g3,initThroughputDataLine())

        testModel.registerThroughPutChanges(DEFAULT_SESSION_ID).observe(requireActivity()) {
            if (it == null || throughput_chart == null) return@observe
            val data = throughput_chart.data ?: return@observe

            val throughPut = ThroughPutDto.convertThroughPutToDto(it)

            val rxdataSet = data.dataSets[ThroughputIndex.RX]
            val txDataSet = data.dataSets[ThroughputIndex.TX]
            data.addEntry(Entry(rxdataSet.entryCount.toFloat(), throughPut.rxResult.toFloat()), ThroughputIndex.RX)
            data.addEntry(Entry(txDataSet.entryCount.toFloat(), throughPut.txResult.toFloat()), ThroughputIndex.TX)

            if (throughput_chart.axisLeft.axisMaximum < throughPut.rxResult || throughput_chart.axisLeft.axisMaximum < throughPut.txResult)
                throughput_chart.axisLeft.axisMaximum = max(throughPut.rxResult, throughPut.txResult).toFloat() + 10f

            // enable touch gestures
            throughput_chart.setTouchEnabled(true)

            // limit the number of visible entries
            throughput_chart.setVisibleXRangeMaximum(10f)

            // move to the latest entry
            throughput_chart.moveViewToX(throughput_chart.data.entryCount.toFloat())

            throughput_chart.data.notifyDataChanged()
            throughput_chart.notifyDataSetChanged()
        }

        initLineChart(serving_cell_chart, lineInitData = initServingCellData(), isNegative = true)
        testModel.registerRadioParametersChanges(DEFAULT_SESSION_ID).observe(requireActivity()) {
            if (it == null || it.isEmpty() || serving_cell_chart == null) return@observe
            val data = serving_cell_chart.data ?: return@observe

            val radioParametersDto = RadioParametersDto.convertRadioParametersToDto(it)

            val rssiDataSet = data.dataSets[ServingCellIndex.RSSI]
            val rsrpDataSet = data.dataSets[ServingCellIndex.RSRP]
            val rsqrDataSet = data.dataSets[ServingCellIndex.RSQR]
            val rssnrDataSet = data.dataSets[ServingCellIndex.RSSNR]

            val servingCell = radioParametersDto.find { current -> current.isServingCell || current.no == 1 }

            data.addEntry(Entry(rssiDataSet.entryCount.toFloat(), servingCell!!.rssi!!.toFloat()), ServingCellIndex.RSSI)
            data.addEntry(Entry(rsrpDataSet.entryCount.toFloat(), servingCell.rsrp!!.toFloat()), ServingCellIndex.RSRP)
            data.addEntry(Entry(rsqrDataSet.entryCount.toFloat(), servingCell.rsrq!!.toFloat()), ServingCellIndex.RSQR)
            data.addEntry(Entry(rssnrDataSet.entryCount.toFloat(), servingCell.rssnr!!.toFloat()), ServingCellIndex.RSSNR)
            Log.v("aaa", "rssi = ${servingCell.rssi},rsrp = ${servingCell.rsrp}, rsqr = ${servingCell.rsrq}, rssnr = ${servingCell.rssnr}")

            val minimumValue = min(min(min(servingCell.rssi!!, servingCell.rsrp), servingCell.rsrq), servingCell.rssnr)
            val maximumValue = max(max(max(servingCell.rssi, servingCell.rsrp), servingCell.rsrq), servingCell.rssnr)

            serving_cell_chart.axisLeft.axisMaximum = maximumValue.toFloat() + 10f
            serving_cell_chart.axisLeft.axisMinimum = minimumValue.toFloat() - 10f

            // enable touch gestures
            serving_cell_chart.setTouchEnabled(true)


            // limit the number of visible entries
            serving_cell_chart.setVisibleXRangeMaximum(10f)

            // move to the latest entry
            serving_cell_chart.moveViewToX(throughput_chart.data.entryCount.toFloat())

            serving_cell_chart.data.notifyDataChanged()
            serving_cell_chart.notifyDataSetChanged()
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
}