package com.isel_5gqos.activities.fragments

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
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
import com.github.mikephil.charting.data.Entry
import com.isel_5gqos.R
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.entities.RadioParameters
import com.isel_5gqos.common.db.entities.Session
import com.isel_5gqos.common.db.entities.ThroughPut
import com.isel_5gqos.factories.TestFactory
import com.isel_5gqos.models.TestViewModel
import com.isel_5gqos.models.observeOnce
import com.isel_5gqos.common.utils.ExcelSheetNamesEnum
import com.isel_5gqos.common.utils.ExcelUtils
import com.isel_5gqos.common.utils.android_utils.AndroidUtils
import com.isel_5gqos.common.utils.mobile_utils.RadioParametersUtils
import com.isel_5gqos.common.utils.mp_android_chart_utils.ChartUtils
import kotlinx.android.synthetic.main.fragment_main_session.*
import kotlinx.android.synthetic.main.fragment_session_details_dialog.*
import kotlinx.android.synthetic.main.fragment_table.*
import java.lang.Long
import java.text.SimpleDateFormat
import java.util.*


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
    private lateinit var sessionStartDate : TextView
    private lateinit var sessionEndDate : TextView


    //<editor-fold name="EVENTS">

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

        btn_export_excel.setOnClickListener {
            testModel.getRadioParametersBySessionId(sessionId = session.id).observeOnce(requireActivity()) { radioParameters ->
                testModel.getThroughputBySessionId(sessionId = session.id).observeOnce(requireActivity()) { throughputs ->
                    ExcelUtils.exportToExcel(
                        context = requireContext(),
                        filename = "${session.id}_session_info",
                        sheetsMap = mapOf(
                            ExcelSheetNamesEnum.RADIO_PARAMETERS.sheetName to Triple(
                                radioParameters,
                                { ExcelUtils.makeRadioParametersHeaderRow(it) },
                                { row, radioParameter -> ExcelUtils.makeRadioParametersRow(row, radioParameter as RadioParameters) }
                            ),
                            ExcelSheetNamesEnum.THROUGHPUT.sheetName to Triple(
                                throughputs,
                                { ExcelUtils.makeThroughputHeaderRow(it) },
                                { row, throughput -> ExcelUtils.makeThroughputRow(row, throughput as ThroughPut) }
                            ),
                        )
                    )
                }
            }
        }
        sessionDetailsThroughputChart = dialogView.findViewById(R.id.session_details_throughput_chart)
        sessionDetailsServingCellChart = dialogView.findViewById(R.id.session_details_serving_cell_chart)
        sessionDetailsStrongestNeighborChart = dialogView.findViewById(R.id.session_details_strongest_neighbor_chart)
        sessionDetailsNumberOfCellsSameTechServingCellChart = dialogView.findViewById(R.id.session_details_nr_cells_same_tech_serving_cell)
        sessionStartDate = dialogView.findViewById(R.id.start_date_txt)
        sessionEndDate = dialogView.findViewById(R.id.end_date_txt)

        ChartUtils.initLineChart(sessionDetailsThroughputChart, lineInitData = ChartUtils.initThroughputDataLine(), drawable = chartBackground)
        ChartUtils.initLineChart(sessionDetailsServingCellChart, lineInitData = ChartUtils.initServingCellData(), isNegative = true, drawable = chartBackground)
        ChartUtils.initLineChart(sessionDetailsStrongestNeighborChart, lineInitData = ChartUtils.initStrongestNeighborData(), isNegative = true, drawable = chartBackground)
        ChartUtils.initLineChart(sessionDetailsNumberOfCellsSameTechServingCellChart, lineInitData = ChartUtils.initNumberOfCells(), isNegative = false, drawable = chartBackground)

        registerObservers()
    }

    //</editor-fold>

    //<editor-fold name="OBSERVERS">
    private fun registerObservers() {
        registerSessionObserver()
        registerThroughPutChartAndObserver()
        registerServingCellChartAndObserver()

    }

    private fun registerSessionObserver(){
        testModel.getSessionInfo(session.id).observeOnce(lifecycleOwner) {
            val format = SimpleDateFormat("yyyy.MM.dd HH:mm")

            val startdate = Date(it.beginDate)
            sessionStartDate.text = format.format(startdate)

            val endDate = Date(it.endDate)
            sessionEndDate.text = format.format(endDate)
        }
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
        testModel.getServingCells(session.id).observeOnce(requireActivity()) {

            if (it == null || it.isEmpty()) return@observeOnce

            sessionDetailsServingCellChart.data ?: return@observeOnce

            var auxLastUpdatedIndex = 0

            Log.v("TESTE",it.size.toString())

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

    //</editor-fold>

    //<editor-fold name="AUX FUNCTIONS">

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

}