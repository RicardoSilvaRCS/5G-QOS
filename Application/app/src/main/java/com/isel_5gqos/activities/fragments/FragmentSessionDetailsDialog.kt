package com.isel_5gqos.activities.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.isel_5gqos.R
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.entities.RadioParameters
import com.isel_5gqos.common.db.entities.Session
import com.isel_5gqos.factories.TestFactory
import com.isel_5gqos.models.TestViewModel
import com.isel_5gqos.models.observeOnce
import com.isel_5gqos.utils.android_utils.AndroidUtils
import com.isel_5gqos.utils.mobile_utils.RadioParametersUtils
import kotlinx.android.synthetic.main.fragment_main_session.*
import kotlinx.android.synthetic.main.fragment_session_details_dialog.*
import java.lang.Long


class FragmentSessionDetailsDialog(val session: Session,private val chartBackground:Drawable,private val lifecycleOwner: LifecycleOwner) : DialogFragment() {
    lateinit var dialog: AlertDialog
    private lateinit var dialogView: View
    private var nrOfClicks = 1

    private lateinit var testFactory: TestFactory
    private val testModel by lazy {
        ViewModelProvider(this, testFactory)[TestViewModel::class.java]
    }

    private lateinit var sessionDetailsThroughputChart: LineChart
    private lateinit var sessionDetailsServingCellChart: LineChart
    private lateinit var sessionDetailsStrongestNeighborChart: LineChart
    private lateinit var sessionDetailsNumberOfCellsSameTechServingCellChart: LineChart


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val username = requireActivity().intent.getStringExtra(USER) ?: ""
        testFactory = TestFactory(savedInstanceState, username)
        dialogView =
            LayoutInflater.from(context).inflate(R.layout.fragment_session_details_dialog, null)

        dialog = AlertDialog.Builder(requireContext(), R.style.FullScreenDialog)
            .setView(dialogView)
            .create()

        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = dialogView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btn_back.setOnClickListener { dismiss() }
        btn_delete.setOnClickListener {
            val dialog = AlertDialog
                .Builder(requireContext())
                .create()
            val inflater = LayoutInflater.from(requireContext())
            val inflatedView = inflater.inflate(resources.getLayout(R.layout.delete_session_alert_dialog), null)
            val confirmButton = inflatedView.findViewById<Button>(R.id.session_delete_confirm_button)

            confirmButton.setOnClickListener {

                val loadingDialog = AndroidUtils.makeLoadingDialog(requireContext(),"Deleting...")
                loadingDialog.show()

                testModel.deleteSessionInfo(session.id,onPostExecute = {loadingDialog.dismiss()})

                dialog.dismiss()
                dismiss()
            }
            val cancelButton = inflatedView.findViewById<Button>(R.id.session_delete_cancel_button)
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }


            dialog.setView(inflatedView)
            dialog.setCanceledOnTouchOutside(false)
            dialog.window!!.setBackgroundDrawableResource(R.drawable.white_background_gradient_blue_500_purple_500_red_border_round_20)
            dialog.show()

        }

        sessionDetailsThroughputChart = dialogView.findViewById(R.id.session_details_throughput_chart)
        sessionDetailsServingCellChart = dialogView.findViewById(R.id.session_details_serving_cell_chart)
        sessionDetailsStrongestNeighborChart = dialogView.findViewById(R.id.session_details_strongest_neighbor_chart)
        sessionDetailsNumberOfCellsSameTechServingCellChart = dialogView.findViewById(R.id.session_details_nr_cells_same_tech_serving_cell)

        initLineChart(sessionDetailsThroughputChart, initThroughputDataLine())
        initLineChart(sessionDetailsServingCellChart, lineInitData = initServingCellData(), isNegative = true)
        initLineChart(sessionDetailsStrongestNeighborChart, lineInitData = initStrongestNeighborData(), isNegative = true)
        initLineChart(sessionDetailsNumberOfCellsSameTechServingCellChart, lineInitData = initNumberOfCells(), isNegative = false)

        registerObservers()
    }


    //<editor-fold name="AUX FUNCTIONS">
    private fun registerObservers() {
        registerThroughPutChartAndObserver()
        registerServingCellChartAndObserver()

    }

    private fun registerThroughPutChartAndObserver() {
        testModel.registerThroughPutChanges(session.id).observeOnce(lifecycleOwner) {
            if (it == null || it.isEmpty()) return@observeOnce
            val data = sessionDetailsThroughputChart.data ?: return@observeOnce

            var auxLastUpdatedValue = 0

            it.forEach { throughput ->

                data.addEntry(Entry(auxLastUpdatedValue.toFloat(), throughput.rxResult.toFloat()), ThroughputIndex.RX)
                data.addEntry(Entry(auxLastUpdatedValue.toFloat(), throughput.txResult.toFloat()), ThroughputIndex.TX)

                if (sessionDetailsThroughputChart.axisLeft.axisMaximum < throughput.rxResult || sessionDetailsThroughputChart.axisLeft.axisMaximum < throughput.txResult)
                    sessionDetailsThroughputChart.axisLeft.axisMaximum = Long.max(throughput.rxResult, throughput.txResult).toFloat() + 10f

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

    private fun registerServingCellChartAndObserver() {
        testModel.getServingCell(session.id).observeOnce(requireActivity()) {

            if (it == null || it.isEmpty() || sessionDetailsServingCellChart == null) return@observeOnce

            sessionDetailsServingCellChart.data ?: return@observeOnce

            var auxLastUpdatedIndex = 0

            it.forEach { servingCell ->

                updateServingCellChart(auxLastUpdatedIndex, servingCell)

                updateStrongestNeighborChart(auxLastUpdatedIndex, servingCell)

                updateNumberOfCellsWithSameTechAsServingCell(auxLastUpdatedIndex, servingCell)

                auxLastUpdatedIndex++
            }

            // enable touch gestures
            sessionDetailsServingCellChart.setTouchEnabled(true)
            sessionDetailsStrongestNeighborChart.setTouchEnabled(true)
            sessionDetailsNumberOfCellsSameTechServingCellChart.setTouchEnabled(true)

            // limit the number of visible entries
            sessionDetailsServingCellChart.setVisibleXRangeMaximum(10f)
            sessionDetailsStrongestNeighborChart.setVisibleXRangeMaximum(10f)
            sessionDetailsNumberOfCellsSameTechServingCellChart.setVisibleXRangeMaximum(10f)

            // move to the latest entry
            sessionDetailsServingCellChart.moveViewToX(sessionDetailsServingCellChart.data.entryCount.toFloat())
            sessionDetailsStrongestNeighborChart.moveViewToX(sessionDetailsStrongestNeighborChart.data.entryCount.toFloat())
            sessionDetailsNumberOfCellsSameTechServingCellChart.moveViewToX(sessionDetailsNumberOfCellsSameTechServingCellChart.data.entryCount.toFloat())

            sessionDetailsServingCellChart.data.notifyDataChanged()
            sessionDetailsStrongestNeighborChart.data.notifyDataChanged()
            sessionDetailsNumberOfCellsSameTechServingCellChart.data.notifyDataChanged()

            sessionDetailsServingCellChart.notifyDataSetChanged()
            sessionDetailsStrongestNeighborChart.notifyDataSetChanged()
            sessionDetailsNumberOfCellsSameTechServingCellChart.notifyDataSetChanged()

        }
    }

    private fun updateServingCellChart(auxLastUpdatedIndex: Int, servingCell: RadioParameters) {

        val servingCellData = sessionDetailsServingCellChart.data ?: return

        servingCellData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rssi.toFloat()), ServingCellIndex.RSSI)
        servingCellData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rsrp.toFloat()), ServingCellIndex.RSRP)
        servingCellData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rsrq.toFloat()), ServingCellIndex.RSQR)
        servingCellData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rssnr.toFloat()), ServingCellIndex.RSSNR)

        val minimumValue = Integer.min(Integer.min(Integer.min(servingCell.rssi, servingCell.rsrp), servingCell.rsrq), servingCell.rssnr)
        val maximumValue = Integer.max(Integer.max(Integer.max(servingCell.rssi, servingCell.rsrp), servingCell.rsrq), servingCell.rssnr)

        sessionDetailsServingCellChart.axisLeft.axisMaximum = maximumValue.toFloat() + 10f
        sessionDetailsServingCellChart.axisLeft.axisMinimum = minimumValue.toFloat() - 10f

    }

    private fun updateStrongestNeighborChart(auxLastUpdatedIndex: Int, servingCell: RadioParameters) {

        val strongestNeighborData = sessionDetailsStrongestNeighborChart.data ?: return

        val cellDataType = RadioParametersUtils.convertStringToNetworkDataType(servingCell.netDataType)

        if (cellDataType == NetworkDataTypesEnum.GSM && servingCell.rssi != MIN_RSSI) {

            strongestNeighborData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rssi.toFloat()), StrongestNeighborIndex.RSSI_GSM)
            sessionDetailsStrongestNeighborChart.axisLeft.axisMinimum = servingCell.rssi.toFloat() - 10f
        } else if (cellDataType == NetworkDataTypesEnum.UMTS && servingCell.rssi != MIN_RSSI) {

            strongestNeighborData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rssi.toFloat()), StrongestNeighborIndex.RSSI_WCDMA)
            sessionDetailsStrongestNeighborChart.axisLeft.axisMinimum = servingCell.rssi.toFloat() - 10f
        } else if (cellDataType == NetworkDataTypesEnum.LTE && servingCell.rsrp != MIN_RSRP) {

            strongestNeighborData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rsrp.toFloat()), StrongestNeighborIndex.RSRP_LTE)
            sessionDetailsStrongestNeighborChart.axisLeft.axisMinimum = servingCell.rsrp.toFloat() - 10f
        }

    }

    private fun updateNumberOfCellsWithSameTechAsServingCell(auxLastUpdatedIndex: Int, servingCell: RadioParameters) {

        val numberOfCells = sessionDetailsNumberOfCellsSameTechServingCellChart.data ?: return

        numberOfCells.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.numbOfCellsWithSameTechAsServing.toFloat()), NumberOfCells.NUMBER)

        sessionDetailsNumberOfCellsSameTechServingCellChart.axisLeft.axisMaximum = servingCell.numbOfCellsWithSameTechAsServing.toFloat() + 10f
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

    private fun initStrongestNeighborData(): LineData {

        val rssiGsmData = java.util.ArrayList<Entry>()
        rssiGsmData.add(Entry(0f, 0f))
        val rssiGsm = LineDataSet(rssiGsmData, "RSSI GSM")
        rssiGsm.lineWidth = 3f
        rssiGsm.circleRadius = 1f
        rssiGsm.highLightColor = Color.rgb(163, 145, 3)
        rssiGsm.setDrawValues(false)


        val rssiWcdmaData = java.util.ArrayList<Entry>()
        rssiWcdmaData.add(Entry(0f, 0f))
        val rssiWcdma = LineDataSet(rssiWcdmaData, "RSSI WCDMA")
        rssiWcdma.lineWidth = 3f
        rssiWcdma.circleRadius = 1f
        rssiWcdma.highLightColor = Color.rgb(0, 255, 0)
        rssiWcdma.color = ColorTemplate.VORDIPLOM_COLORS[1]
        rssiWcdma.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[1])
        rssiWcdma.setDrawValues(false)

        val rsrpLteData = java.util.ArrayList<Entry>()
        rsrpLteData.add(Entry(0f, 0f))
        val rsrpLte = LineDataSet(rsrpLteData, "RSRP LTE")
        rsrpLte.lineWidth = 3f
        rsrpLte.circleRadius = 1f
        rsrpLte.highLightColor = Color.rgb(0, 0, 255)
        rsrpLte.color = ColorTemplate.VORDIPLOM_COLORS[2]
        rsrpLte.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[2])
        rsrpLte.setDrawValues(false)


        val sets = java.util.ArrayList<ILineDataSet>()
        sets.add(rssiGsm)
        sets.add(rssiWcdma)
        sets.add(rsrpLte)

        return LineData(sets)
    }

    private fun initNumberOfCells(): LineData {

        val numberOfCells = java.util.ArrayList<Entry>()

        numberOfCells.add(Entry(0f, 0f))

        val servingCellTechNumberData = java.util.ArrayList<Entry>()

        servingCellTechNumberData.add(Entry(0f, 0f))
        val servingCellTechNumber = LineDataSet(servingCellTechNumberData, "Number(cells with same tech as serving)")
        servingCellTechNumber.lineWidth = 3f
        servingCellTechNumber.circleRadius = 1f
        servingCellTechNumber.highLightColor = Color.rgb(255, 0, 0)
        servingCellTechNumber.color = ColorTemplate.VORDIPLOM_COLORS[0]
        servingCellTechNumber.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0])
        servingCellTechNumber.setDrawValues(false)


        val sets = java.util.ArrayList<ILineDataSet>()
        sets.add(servingCellTechNumber)

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

    //</editor-fold>
}