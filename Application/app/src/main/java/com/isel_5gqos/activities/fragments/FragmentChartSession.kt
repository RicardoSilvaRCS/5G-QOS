package com.isel_5gqos.activities.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
import com.isel_5gqos.factories.TestFactory
import com.isel_5gqos.models.TestViewModel
import com.isel_5gqos.utils.mobile_utils.RadioParametersUtils
import com.isel_5gqos.utils.publisher_subscriber.MessageEvent
import com.isel_5gqos.utils.publisher_subscriber.StringMessageEvent
import kotlinx.android.synthetic.main.fragment_main_session.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Integer.max
import java.lang.Integer.min
import java.lang.Long.max

class FragmentChartSession : Fragment() {

    /**INIT UI ELEMENTS**/
    private lateinit var testFactory: TestFactory
    private val testModel by lazy {
        ViewModelProvider(this,testFactory)[TestViewModel::class.java]
    }

    //<editor-fold name="EVENTS">
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_main_session, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val username = requireActivity().intent.getStringExtra(USER) ?: ""
        testFactory = TestFactory(savedInstanceState,username)
        registerObservers()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop()
    }
    //</editor-fold>

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(messageEvent: MessageEvent) {
        if (messageEvent !is StringMessageEvent) return
        registerObservers(messageEvent.message)
    }

    //<editor-fold name="OBSERVERS"
    private fun registerObservers(sessionId: String = DEFAULT_SESSION_ID) {
        initLineChart(lineChart = throughput_chart, lineInitData = initThroughputDataLine(), isNegative = false)
        initLineChart(lineChart = serving_cell_chart, lineInitData = initServingCellData(), isNegative = true)
        initLineChart(lineChart = strongest_neighbor, lineInitData = initStrongestNeighborData(), isNegative = true)
        initLineChart(lineChart = number_of_cells_same_tech_as_serving, lineInitData = initNumberOfCells(), isNegative = false)

        registerServingCellChanges(sessionId)
        registerThroughputObserver(sessionId)
    }

    private fun registerThroughputObserver(sessionId: String) {

        testModel.registerThroughPutChanges(sessionId).observe(requireActivity()) {
            if (!checkIfLayoutsAreAvailable() || it == null || it.isEmpty() || throughput_chart == null) return@observe
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

    private fun registerServingCellChanges(sessionId: String) {

        testModel.getServingCell(sessionId).observe(requireActivity()) {

            if (!checkIfLayoutsAreAvailable() || it == null || it.isEmpty() || serving_cell_chart == null) return@observe

            val servingCellData = serving_cell_chart.data ?: return@observe

            var auxLastUpdatedIndex = min(servingCellData.dataSets[ServingCellIndex.RSSI].entryCount, it.size - 1)

            it.subList(auxLastUpdatedIndex, it.size).forEach { servingCell ->

                updateServingCellChart(auxLastUpdatedIndex, servingCell)

                updateStrongestNeighborChart(auxLastUpdatedIndex, servingCell)

                updateNumberOfCellsWithSameTechAsServingCell(auxLastUpdatedIndex, servingCell)

                auxLastUpdatedIndex++
            }

            // enable touch gestures
            serving_cell_chart.setTouchEnabled(true)
            strongest_neighbor.setTouchEnabled(true)
            number_of_cells_same_tech_as_serving.setTouchEnabled(true)

            // limit the number of visible entries
            serving_cell_chart.setVisibleXRangeMaximum(10f)
            strongest_neighbor.setVisibleXRangeMaximum(10f)
            number_of_cells_same_tech_as_serving.setVisibleXRangeMaximum(10f)

            // move to the latest entry
            serving_cell_chart.moveViewToX(serving_cell_chart.data.entryCount.toFloat())
            serving_cell_chart.data.notifyDataChanged()
            serving_cell_chart.notifyDataSetChanged()

            strongest_neighbor.moveViewToX(strongest_neighbor.data.entryCount.toFloat())
            strongest_neighbor.data.notifyDataChanged()
            strongest_neighbor.notifyDataSetChanged()

            number_of_cells_same_tech_as_serving.moveViewToX(strongest_neighbor.data.entryCount.toFloat())
            number_of_cells_same_tech_as_serving.data.notifyDataChanged()
            number_of_cells_same_tech_as_serving.notifyDataSetChanged()

        }

    }

    //</editor-fold>

    //<editor-fold name="AUX FUNCTIONS">

    private fun checkIfLayoutsAreAvailable() = this.isResumed

    private fun updateServingCellChart(auxLastUpdatedIndex: Int, servingCell: RadioParameters) {

        val servingCellData = serving_cell_chart.data ?: return

        servingCellData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rssi.toFloat()), ServingCellIndex.RSSI)
        servingCellData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rsrp.toFloat()), ServingCellIndex.RSRP)
        servingCellData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rsrq.toFloat()), ServingCellIndex.RSQR)
        servingCellData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rssnr.toFloat()), ServingCellIndex.RSSNR)

        val minimumValue = min(min(min(servingCell.rssi, servingCell.rsrp), servingCell.rsrq), servingCell.rssnr)
        val maximumValue = max(max(max(servingCell.rssi, servingCell.rsrp), servingCell.rsrq), servingCell.rssnr)

        serving_cell_chart.axisLeft.axisMaximum = maximumValue.toFloat() + 10f
        serving_cell_chart.axisLeft.axisMinimum = minimumValue.toFloat() - 10f

    }

    private fun updateStrongestNeighborChart(auxLastUpdatedIndex: Int, servingCell: RadioParameters) {

        val strongestNeighborData = strongest_neighbor.data ?: return

        val cellDataType = RadioParametersUtils.convertStringToNetworkDataType(servingCell.netDataType)

        if (cellDataType == NetworkDataTypesEnum.GSM && servingCell.rssi != MIN_RSSI) {

            strongestNeighborData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rssi.toFloat()), StrongestNeighborIndex.RSSI_GSM)
            strongest_neighbor.axisLeft.axisMinimum = servingCell.rssi.toFloat() - 10f
        } else if (cellDataType == NetworkDataTypesEnum.UMTS && servingCell.rssi != MIN_RSSI) {

            strongestNeighborData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rssi.toFloat()), StrongestNeighborIndex.RSSI_WCDMA)
            strongest_neighbor.axisLeft.axisMinimum = servingCell.rssi.toFloat() - 10f
        } else if (cellDataType == NetworkDataTypesEnum.LTE && servingCell.rsrp != MIN_RSRP) {

            strongestNeighborData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rsrp.toFloat()), StrongestNeighborIndex.RSRP_LTE)
            strongest_neighbor.axisLeft.axisMinimum = servingCell.rsrp.toFloat() - 10f
        }

    }

    private fun updateNumberOfCellsWithSameTechAsServingCell(auxLastUpdatedIndex: Int, servingCell: RadioParameters) {

        val numberOfCells = number_of_cells_same_tech_as_serving.data ?: return

        numberOfCells.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.numbOfCellsWithSameTechAsServing.toFloat()), NumberOfCells.NUMBER)

        number_of_cells_same_tech_as_serving.axisLeft.axisMaximum = servingCell.numbOfCellsWithSameTechAsServing.toFloat() + 10f
    }

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