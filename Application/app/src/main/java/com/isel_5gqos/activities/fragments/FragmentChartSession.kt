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
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.isel_5gqos.QosApp
import com.isel_5gqos.R
import com.isel_5gqos.common.DEFAULT_SESSION_ID
import com.isel_5gqos.common.ServingCellIndex
import com.isel_5gqos.common.ThroughputIndex
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.jobs.WorkTypesEnum
import com.isel_5gqos.jobs.scheduleJob
import com.isel_5gqos.models.InternetViewModel
import com.isel_5gqos.models.TestViewModel
import kotlinx.android.synthetic.main.fragment_main_session.*
import java.lang.Integer.max
import java.lang.Integer.min
import java.lang.Long.max

class FragmentChartSession : Fragment() {

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
        initLineChart(throughput_chart, initThroughputDataLine())

        initLineChart(g1, initThroughputDataLine())
        initLineChart(g2, initThroughputDataLine())
        initLineChart(g3, initThroughputDataLine())

        registerObservers()
    }

    //<editor-fold name="OBSERVERS"
    private fun registerObservers(){
        registerRadioParametersObserver()
        registerThroughputObserver()
    }

    private fun registerThroughputObserver(){
        testModel.registerThroughPutChanges(DEFAULT_SESSION_ID).observe(requireActivity()) {
            if (!checkIfLayoutsAreAvailable() ||it == null || it.isEmpty() || throughput_chart == null) return@observe
            val data = throughput_chart.data ?: return@observe

            var auxLastUpdatedValue = min(data.dataSets[ThroughputIndex.RX].entryCount, it.size - 1)

            it.subList(auxLastUpdatedValue, it.size).forEach { throughput ->

                data.addEntry(Entry(auxLastUpdatedValue.toFloat(), throughput.rxResult.toFloat()), ThroughputIndex.RX)
                data.addEntry(Entry(auxLastUpdatedValue.toFloat(), throughput.txResult.toFloat()), ThroughputIndex.TX)

                if (throughput_chart.axisLeft.axisMaximum < throughput.rxResult || throughput_chart.axisLeft.axisMaximum < throughput.txResult)
                    throughput_chart.axisLeft.axisMaximum = max(throughput.rxResult, throughput.txResult).toFloat() + 10f

                auxLastUpdatedValue++
            }

            // enable touch gestures
            throughput_chart.setTouchEnabled(true)

            // limit the number of visible entries
            throughput_chart.setVisibleXRangeMaximum(10f)

            // move to the latest entry
            throughput_chart.moveViewToX(throughput_chart.data.entryCount.toFloat())

            throughput_chart.data.notifyDataChanged()
            throughput_chart.notifyDataSetChanged()
        }
    }

    private fun registerRadioParametersObserver(){
        initLineChart(serving_cell_chart, lineInitData = initServingCellData(), isNegative = true)
        testModel.getServingCell(DEFAULT_SESSION_ID).observe(requireActivity()) {
            if (!checkIfLayoutsAreAvailable() || it == null || it.isEmpty() || serving_cell_chart == null) return@observe
            val data = serving_cell_chart.data ?: return@observe

            var auxLastUpdatedIndex = min(data.dataSets[ServingCellIndex.RSSI].entryCount, it.size - 1)

            it.subList(auxLastUpdatedIndex, it.size).forEach { servingCell ->
                data.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rssi.toFloat()), ServingCellIndex.RSSI)
                data.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rsrp.toFloat()), ServingCellIndex.RSRP)
                data.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rsrq.toFloat()), ServingCellIndex.RSQR)
                data.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rssnr.toFloat()), ServingCellIndex.RSSNR)

                Log.v("aaa", "rssi = ${servingCell.rssi},rsrp = ${servingCell.rsrp}, rsqr = ${servingCell.rsrq}, rssnr = ${servingCell.rssnr}")

                val minimumValue = min(min(min(servingCell.rssi, servingCell.rsrp), servingCell.rsrq), servingCell.rssnr)
                val maximumValue = max(max(max(servingCell.rssi, servingCell.rsrp), servingCell.rsrq), servingCell.rssnr)

                serving_cell_chart.axisLeft.axisMaximum = maximumValue.toFloat() + 10f
                serving_cell_chart.axisLeft.axisMinimum = minimumValue.toFloat() - 10f

                auxLastUpdatedIndex++
            }

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

    //</editor-fold>

    //<editor-fold name="AUX FUNCTIONS">

    private fun checkIfLayoutsAreAvailable() = this.isResumed

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

    //<editor-fold name="CHART FUNCTIONS">
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
        rssi.lineWidth = 3f
        rssi.circleRadius = 1f
        rssi.highLightColor = Color.rgb(163, 145, 3)
        rssi.setDrawValues(false)


        val rsrpValue = java.util.ArrayList<Entry>()
        rsrpValue.add(Entry(0f, 0f))
        val rsrp = LineDataSet(rsrpValue, "RSRP")
        rsrp.lineWidth = 3f
        rsrp.circleRadius = 1f
        rsrp.highLightColor = Color.rgb(0, 255, 0)
        rsrp.color = ColorTemplate.VORDIPLOM_COLORS[1]
        rsrp.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[1])
        rsrp.setDrawValues(false)

        val rsqrValue = java.util.ArrayList<Entry>()
        rsqrValue.add(Entry(0f, 0f))
        val rsqr = LineDataSet(rsqrValue, "RSQR")
        rsqr.lineWidth = 3f
        rsqr.circleRadius = 1f
        rsqr.highLightColor = Color.rgb(0, 0, 255)
        rsqr.color = ColorTemplate.VORDIPLOM_COLORS[2]
        rsqr.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[2])
        rsqr.setDrawValues(false)


        val rssnrValue = java.util.ArrayList<Entry>()
        rssnrValue.add(Entry(0f, 0f))
        val rssnr = LineDataSet(rssnrValue, "RSSNR")
        rssnr.lineWidth = 3f
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

        lineChart.background = resources.getDrawable(R.drawable.white_background_round_20)
        // disable description text
        lineChart.description.isEnabled = false

        // enable scaling and dragging
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setDrawGridBackground(false)

        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.setPinchZoom(true)

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.axisLineWidth = 1.5f
        xAxis.setDrawGridLines(false)
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.isEnabled = true
        xAxis.granularity = granularity

        val yAxis = lineChart.axisLeft
        yAxis.axisLineWidth = 1.5f
        yAxis.setDrawGridLines(true)
        yAxis.axisMaximum = if (isNegative) 0f else 10f
        yAxis.axisMinimum = if (isNegative) -10f else 0f

        lineChart.axisRight.isEnabled = false

        lineChart.data = lineInitData
    }
    //</editor-fold>
    //</editor-fold>
}