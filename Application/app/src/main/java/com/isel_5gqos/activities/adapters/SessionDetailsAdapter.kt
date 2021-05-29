package com.isel_5gqos.activities.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.card.MaterialCardView
import com.isel_5gqos.R
import com.isel_5gqos.common.DEFAULT_SESSION_ID
import com.isel_5gqos.common.ServingCellIndex
import com.isel_5gqos.common.ThroughputIndex
import com.isel_5gqos.common.db.entities.Session
import com.isel_5gqos.models.TestViewModel
import kotlinx.android.synthetic.main.fragment_controlled_session.*
import kotlinx.android.synthetic.main.fragment_main_session.*
import kotlinx.android.synthetic.main.fragment_session_details_dialog.*
import java.lang.Long.max
import java.text.SimpleDateFormat
import java.util.*

class SessionDetailsAdapter(
    private val sessions: List<Session>,
    private val dialog: DialogFragment,
    private val parentFragmentManager: FragmentManager,
    private val lifecycleOwner: LifecycleOwner,
    private val testModel: TestViewModel,
    private val chartBackground: Drawable,
    private val displayMetrics: DisplayMetrics
) : RecyclerView.Adapter<SessionDetailsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionDetailsViewHolder {
        val sessionLayout = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.session_details_recycler_view_adapter, null) as LinearLayout

        return SessionDetailsViewHolder(
            sessionLayout,
            dialog,
            parentFragmentManager,
            lifecycleOwner,
            testModel,
            chartBackground,
            parent.context,
            displayMetrics
        )
    }

    override fun onBindViewHolder(holder: SessionDetailsViewHolder, position: Int) {
        holder.bindTo(sessions[position])
    }

    override fun getItemCount(): Int = sessions.size
}


class SessionDetailsViewHolder(
    private val view: LinearLayout,
    dialog: DialogFragment,
    private val parentFragmentManager: FragmentManager,
    private val lifecycleOwner: LifecycleOwner,
    private val testModel: TestViewModel,
    private val chartBackground: Drawable,
    private val context: Context,
    private val displayMetrics: DisplayMetrics
) : RecyclerView.ViewHolder(view) {
    private lateinit var session: Session

    private lateinit var sessionDetailsThroughputChart: LineChart
    private lateinit var sessionDetailsServingCellChart: LineChart

    private val sessionNameTextView = view.findViewById<TextView>(R.id.session_details_name)
    private val sessionStartDateTextView = view.findViewById<TextView>(R.id.session_start_date)
    private val sessionDurationTextView = view.findViewById<TextView>(R.id.session_duration)

    fun bindTo(session: Session) {

        this.session = session
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm")

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = session.beginDate

        sessionNameTextView.text = session.sessionName
        sessionStartDateTextView.text = formatter.format(calendar.time).toString()
        sessionDurationTextView.text = "${(session.endDate - session.beginDate) / 1000} sec."
        view.layoutParams = LinearLayout.LayoutParams((displayMetrics.widthPixels * 0.9).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    init {

        val card = view.findViewById<MaterialCardView>(R.id.session_card)
        card.setOnClickListener {
            dialog.show(parentFragmentManager, "DIALOG_TAG")
            val dialogView = LayoutInflater
                .from(context)
                .inflate(R.layout.fragment_session_details_dialog, null)

            sessionDetailsThroughputChart = dialogView.findViewById(R.id.session_details_throughput_chart)
            sessionDetailsServingCellChart = dialogView.findViewById(R.id.session_details_serving_cell_chart)

            initLineChart(sessionDetailsThroughputChart, initThroughputDataLine())
            initLineChart(sessionDetailsServingCellChart, lineInitData = initServingCellData(), isNegative = true)

            registerObservers()
        }
    }

    private fun registerObservers() {
        registerThroughPutChartAndObserver()
        registerRadioParametersChartAndObserver()
        registerServingCellChartAndObserver()

    }

    private fun registerThroughPutChartAndObserver() {
        testModel.registerThroughPutChanges(session.id).observe(lifecycleOwner) {
            if (it == null || it.isEmpty()) return@observe
            val data = sessionDetailsThroughputChart.data ?: return@observe

            var auxLastUpdatedValue = Integer.min(data.dataSets[ThroughputIndex.RX].entryCount, it.size - 1)

            it.subList(auxLastUpdatedValue, it.size).forEach { throughput ->

                data.addEntry(Entry(auxLastUpdatedValue.toFloat(), throughput.rxResult.toFloat()), ThroughputIndex.RX)
                data.addEntry(Entry(auxLastUpdatedValue.toFloat(), throughput.txResult.toFloat()), ThroughputIndex.TX)

                if (sessionDetailsThroughputChart.axisLeft.axisMaximum < throughput.rxResult || sessionDetailsThroughputChart.axisLeft.axisMaximum < throughput.txResult)
                    sessionDetailsThroughputChart.axisLeft.axisMaximum = max(throughput.rxResult, throughput.txResult).toFloat() + 10f

                auxLastUpdatedValue++
            }

            // enable touch gestures
            sessionDetailsThroughputChart.setTouchEnabled(true)

            // limit the number of visible entries
            sessionDetailsThroughputChart.setVisibleXRangeMaximum(10f)

            // move to the latest entry
            sessionDetailsThroughputChart.moveViewToX(sessionDetailsThroughputChart.data.entryCount.toFloat())

            sessionDetailsThroughputChart.data.notifyDataChanged()
            sessionDetailsThroughputChart.notifyDataSetChanged()
        }
    }

    private fun registerRadioParametersChartAndObserver() {
        initLineChart(sessionDetailsServingCellChart, lineInitData = initServingCellData(), isNegative = true)
        testModel.getServingCell(DEFAULT_SESSION_ID).observe(lifecycleOwner) {
            if (it == null || it.isEmpty()) return@observe
            val data = sessionDetailsServingCellChart.data ?: return@observe

            var auxLastUpdatedIndex = Integer.min(data.dataSets[ServingCellIndex.RSSI].entryCount, it.size - 1)

            it.subList(auxLastUpdatedIndex, it.size).forEach { servingCell ->
                data.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rssi.toFloat()), ServingCellIndex.RSSI)
                data.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rsrp.toFloat()), ServingCellIndex.RSRP)
                data.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rsrq.toFloat()), ServingCellIndex.RSQR)
                data.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rssnr.toFloat()), ServingCellIndex.RSSNR)

                Log.v("aaa", "rssi = ${servingCell.rssi},rsrp = ${servingCell.rsrp}, rsqr = ${servingCell.rsrq}, rssnr = ${servingCell.rssnr}")

                val minimumValue = Integer.min(Integer.min(Integer.min(servingCell.rssi, servingCell.rsrp), servingCell.rsrq), servingCell.rssnr)
                val maximumValue = Integer.max(Integer.max(Integer.max(servingCell.rssi, servingCell.rsrp), servingCell.rsrq), servingCell.rssnr)

                sessionDetailsServingCellChart.axisLeft.axisMaximum = maximumValue.toFloat() + 10f
                sessionDetailsServingCellChart.axisLeft.axisMinimum = minimumValue.toFloat() - 10f

                auxLastUpdatedIndex++
            }

            // enable touch gestures
            sessionDetailsServingCellChart.setTouchEnabled(true)
            // limit the number of visible entries
            sessionDetailsServingCellChart.setVisibleXRangeMaximum(10f)

            // move to the latest entry
            sessionDetailsServingCellChart.moveViewToX(sessionDetailsServingCellChart.data.entryCount.toFloat())

            sessionDetailsServingCellChart.data.notifyDataChanged()
            sessionDetailsServingCellChart.notifyDataSetChanged()
        }
    }

    private fun registerServingCellChartAndObserver() {

    }

    //</editor-fold>

    //<editor-fold name="CHART FUNCTIONS">
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


    private fun initLineChart(lineChart: LineChart, lineInitData: LineData, isNegative: Boolean = false, granularity: Float = 1f) {

        lineChart.background = chartBackground
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

}