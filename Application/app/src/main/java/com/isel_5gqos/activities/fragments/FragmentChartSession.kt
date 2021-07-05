package com.isel_5gqos.activities.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.data.Entry
import com.isel_5gqos.R
import com.isel_5gqos.common.*
import com.isel_5gqos.common.enums.NetworkDataTypesEnum
import com.isel_5gqos.common.db.entities.RadioParameters
import com.isel_5gqos.common.db.entities.ThroughPut
import com.isel_5gqos.factories.TestFactory
import com.isel_5gqos.models.TestViewModel
import com.isel_5gqos.common.utils.mobile_utils.RadioParametersUtils
import com.isel_5gqos.common.utils.mp_android_chart_utils.ChartUtils.Companion.initLineChart
import com.isel_5gqos.common.utils.mp_android_chart_utils.ChartUtils.Companion.initNumberOfCells
import com.isel_5gqos.common.utils.mp_android_chart_utils.ChartUtils.Companion.initServingCellData
import com.isel_5gqos.common.utils.mp_android_chart_utils.ChartUtils.Companion.initStrongestNeighborData
import com.isel_5gqos.common.utils.mp_android_chart_utils.ChartUtils.Companion.initThroughputDataLine
import com.isel_5gqos.common.utils.publisher_subscriber.MessageEvent
import com.isel_5gqos.common.utils.publisher_subscriber.StringMessageEvent
import kotlinx.android.synthetic.main.fragment_main_session.*
import kotlinx.android.synthetic.main.fragment_table.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Integer.max
import java.lang.Integer.min
import java.lang.Long

class FragmentChartSession : Fragment() {

    /**INIT UI ELEMENTS**/

    private lateinit var testFactory: TestFactory
    private var sessionId : String = DEFAULT_SESSION_ID
    private val testModel by lazy {
        ViewModelProvider(this,testFactory)[TestViewModel::class.java]
    }

    private var throughputLiveData: LiveData<List<ThroughPut>>? = null
    private var servingCellLiveData: LiveData<List<RadioParameters>>? = null

    //<editor-fold name="EVENTS">
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_main_session, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val username = requireActivity().intent.getStringExtra(USER) ?: ""
        testFactory = TestFactory(savedInstanceState,username)
    }

    override fun onStart() {
        super.onStart()

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        registerObservers(sessionId)
    }

    override fun onStop() {
        resetObservers()
        resetCharts()

        super.onStop()
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(messageEvent: MessageEvent) {
        if (messageEvent !is StringMessageEvent) return

        sessionId = messageEvent.message
        resetObservers()
        resetCharts()
        registerObservers(sessionId)
    }

    //</editor-fold>

    //<editor-fold name="OBSERVERS"
    private fun registerObservers(sessionId: String = DEFAULT_SESSION_ID) {

        initLineChart(lineChart = throughput_chart, lineInitData = initThroughputDataLine(), isNegative = false, drawable = resources.getDrawable(R.drawable.white_background_round_20))
        initLineChart(lineChart = serving_cell_chart, lineInitData = initServingCellData(), isNegative = true, drawable = resources.getDrawable(R.drawable.white_background_round_20))
        initLineChart(lineChart = strongest_neighbor_chart, lineInitData = initStrongestNeighborData(), isNegative = true, drawable = resources.getDrawable(R.drawable.white_background_round_20))
        initLineChart(lineChart = number_of_cells_same_tech_as_serving_chart, lineInitData = initNumberOfCells(), isNegative = false, drawable = resources.getDrawable(R.drawable.white_background_round_20))

        registerServingCellChanges(sessionId)
        registerThroughputObserver(sessionId)
    }

    private fun resetObservers() {
        Log.v("SESSION","resetted")
        throughputLiveData?.removeObservers(requireActivity())
        servingCellLiveData?.removeObservers(requireActivity())
    }

    private fun registerThroughputObserver(sessionId: String) {
        throughputLiveData = testModel.registerThroughPutChanges(sessionId)

        throughputLiveData?.observe(requireActivity()) {
            if (!checkIfLayoutsAreAvailable() || it == null || it.isEmpty() || throughput_chart == null) return@observe
            val data = throughput_chart.data ?: return@observe

            var auxLastUpdatedValue = min(data.dataSets[ThroughputIndex.RX].entryCount, it.size - 1)

            it.subList(auxLastUpdatedValue, it.size).forEach { throughput ->

                data.addEntry(Entry(auxLastUpdatedValue.toFloat(), throughput.rxResult.toFloat()), ThroughputIndex.RX)
                data.addEntry(Entry(auxLastUpdatedValue.toFloat(), throughput.txResult.toFloat()), ThroughputIndex.TX)

                if (throughput_chart.axisLeft.axisMaximum < throughput.rxResult || throughput_chart.axisLeft.axisMaximum < throughput.txResult)
                    throughput_chart.axisLeft.axisMaximum = Long.max(throughput.rxResult, throughput.txResult).toFloat() + 10f

                auxLastUpdatedValue++
                Log.v("SESSION","$sessionId to ${throughput.sessionId}")
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
        servingCellLiveData = testModel.getServingCells(sessionId)

        servingCellLiveData?.observe(requireActivity()){

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
            strongest_neighbor_chart.setTouchEnabled(true)
            number_of_cells_same_tech_as_serving_chart.setTouchEnabled(true)

            // limit the number of visible entries
            serving_cell_chart.setVisibleXRangeMaximum(10f)
            strongest_neighbor_chart.setVisibleXRangeMaximum(10f)
            number_of_cells_same_tech_as_serving_chart.setVisibleXRangeMaximum(10f)

            // move to the latest entry
            serving_cell_chart.moveViewToX(serving_cell_chart.data.entryCount.toFloat())
            strongest_neighbor_chart.moveViewToX(strongest_neighbor_chart.data.entryCount.toFloat())
            number_of_cells_same_tech_as_serving_chart.moveViewToX(strongest_neighbor_chart.data.entryCount.toFloat())

            serving_cell_chart.data.notifyDataChanged()
            strongest_neighbor_chart.data.notifyDataChanged()
            number_of_cells_same_tech_as_serving_chart.data.notifyDataChanged()

            serving_cell_chart.notifyDataSetChanged()
            strongest_neighbor_chart.notifyDataSetChanged()
            number_of_cells_same_tech_as_serving_chart.notifyDataSetChanged()

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

        val strongestNeighborData = strongest_neighbor_chart.data ?: return

        val cellDataType = RadioParametersUtils.convertStringToNetworkDataType(servingCell.netDataType)

        if (cellDataType == NetworkDataTypesEnum.GSM && servingCell.rssi != MIN_RSSI) {

            strongestNeighborData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rssi.toFloat()), StrongestNeighborIndex.RSSI_GSM)
            strongest_neighbor_chart.axisLeft.axisMinimum = servingCell.rssi.toFloat() - 10f
        } else if (cellDataType == NetworkDataTypesEnum.UMTS && servingCell.rssi != MIN_RSSI) {

            strongestNeighborData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rssi.toFloat()), StrongestNeighborIndex.RSSI_WCDMA)
            strongest_neighbor_chart.axisLeft.axisMinimum = servingCell.rssi.toFloat() - 10f
        } else if (cellDataType == NetworkDataTypesEnum.LTE && servingCell.rsrp != MIN_RSRP) {

            strongestNeighborData.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.rsrp.toFloat()), StrongestNeighborIndex.RSRP_LTE)
            strongest_neighbor_chart.axisLeft.axisMinimum = servingCell.rsrp.toFloat() - 10f
        }

    }

    private fun updateNumberOfCellsWithSameTechAsServingCell(auxLastUpdatedIndex: Int, servingCell: RadioParameters) {

        val numberOfCells = number_of_cells_same_tech_as_serving_chart.data ?: return

        numberOfCells.addEntry(Entry(auxLastUpdatedIndex.toFloat(), servingCell.numbOfCellsWithSameTechAsServing.toFloat()), NumberOfCells.NUMBER)

        number_of_cells_same_tech_as_serving_chart.axisLeft.axisMaximum = servingCell.numbOfCellsWithSameTechAsServing.toFloat() + 10f
    }

    private fun resetCharts(){

        throughput_chart.data?.clearValues()
        throughput_chart.invalidate()
        throughput_chart.clear()

        serving_cell_chart.data?.clearValues()
        serving_cell_chart.invalidate()
        serving_cell_chart.clear()

        strongest_neighbor_chart.data?.clearValues()
        strongest_neighbor_chart.invalidate()
        strongest_neighbor_chart.clear()

        number_of_cells_same_tech_as_serving_chart.data?.clearValues()
        number_of_cells_same_tech_as_serving_chart.invalidate()
        number_of_cells_same_tech_as_serving_chart.clear()
    }

    //</editor-fold>
}