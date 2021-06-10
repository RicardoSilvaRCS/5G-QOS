package com.isel_5gqos.activities.fragments

import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.isel_5gqos.R
import com.isel_5gqos.common.NetworkDataTypesEnum
import com.isel_5gqos.common.USER
import com.isel_5gqos.common.db.entities.Location
import com.isel_5gqos.common.db.entities.RadioParameters
import com.isel_5gqos.factories.TestFactory
import com.isel_5gqos.models.TestViewModel
import com.isel_5gqos.utils.mobile_utils.RadioParametersUtils
import com.isel_5gqos.utils.publisher_subscriber.MessageEvent
import com.isel_5gqos.utils.publisher_subscriber.StringMessageEvent
import kotlinx.android.synthetic.main.fragment_main_session.*
import kotlinx.android.synthetic.main.fragment_table.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class FragmentTable : Fragment(){

    private lateinit var testFactory: TestFactory
    private val testModel by lazy {
        ViewModelProvider(this,testFactory).get(TestViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if(this.isAdded) return
        super.onCreate(savedInstanceState)
    }

    private var servingCellLiveData : LiveData<RadioParameters>? = null
    private var radioParametersLiveData : LiveData<List<RadioParameters>>? = null
    private var lastLocationLiveData : LiveData<Location>? = null

    //<editor-fold desc="EVENTS">
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_table, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val username = requireActivity().intent.getStringExtra(USER) ?: ""
        testFactory = TestFactory(savedInstanceState,username)
        distance_txt?.text = "---"
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(messageEvent: MessageEvent) {
        if (messageEvent !is StringMessageEvent) return

        resetObservers()
        registerObservers(messageEvent.message)
    }

    //</editor-fold>

    //<editor-fold desc="OBSERVERS"
    private fun registerObservers(sessionId: String) {

        registerServingCellObserver(sessionId)
        registerRadioParametersTableObserver(sessionId)

    }

    private fun resetObservers() {
        servingCellLiveData?.removeObservers(requireActivity())
        radioParametersLiveData?.removeObservers(requireActivity())
        lastLocationLiveData?.removeObservers(requireActivity())
    }

    //</editor-fold>

    //<editor-fold desc="AUX FUNCTIONS"

    private  fun registerServingCellObserver(sessionId: String) {
        servingCellLiveData = testModel.getServingCell(sessionId)

        servingCellLiveData?.observe(requireActivity()) {
            if(!checkIfLayoutsAreAvailable() || it == null) return@observe
            val servingCell = it
            val telephonyManager = requireContext().getSystemService(TelephonyManager::class.java) as TelephonyManager
            val networkOperator = telephonyManager.networkOperator

            val latitude = if(servingCell.latitude.isEmpty()) "----" else servingCell.latitude
            val longitude = if(servingCell.longitude.isEmpty()) "----" else servingCell.longitude

            lat_lon_txt.text = "$latitude/$longitude"

            serving_cell_tech.text = servingCell.tech
            rsrp_txt.text = servingCell.rsrp.toString()
            arfcn_txt.text = servingCell.arfcn.toString()
            rsrq_txt.text = servingCell.rsrq.toString()
            rssnr_txt.text = servingCell.rssnr.toString()
            net_data_type_txt.text = NetworkDataTypesEnum.valueOf(servingCell.netDataType.toUpperCase()).toString()
            mcc_txt.text = "${networkOperator.substring(0, 3)} ${networkOperator.substring(3)}"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tac_txt.text = telephonyManager.typeAllocationCode
            } else {
                tac_txt.text = "---"
            }

            val cellidHex: String = decToHex(servingCell.cId) ?: ""
            val eNBHex = cellidHex.substring(0, cellidHex.length - 2)
            val eNB: Int = hexToDec(eNBHex)

            e_node_b_txt.text = eNB.toString()
            pci_txt.text = servingCell.pci.toString()

            sim_operator_txt.text = "${telephonyManager.simOperatorName}/${telephonyManager.networkOperatorName}"

        }
    }

    private fun registerRadioParametersTableObserver(sessionId: String) {

        val layoutInflater = LayoutInflater.from(requireContext())

        radioParametersLiveData = testModel.registerRadioParametersChanges(sessionId)

        radioParametersLiveData?.observe(requireActivity()) {
            if(!checkIfLayoutsAreAvailable() || neighbors_table_layout == null) return@observe

            it
                .filter { cell -> cell.no != 1 }
                .forEachIndexed { index, radioParametersDto ->

                    val tableRow =
                        layoutInflater.inflate(R.layout.neighbors_table_row, null).findViewById<TableRow>(R.id.neighbor_row) ?: return@observe
                    (tableRow[0] as TextView).text = radioParametersDto.no.toString()
                    (tableRow[2] as TextView).text = radioParametersDto.tech
                    (tableRow[4] as TextView).text = radioParametersDto.arfcn.toString()
                    (tableRow[6] as TextView).text = RadioParametersUtils.getReferenceStrength(radioParametersDto).toString()
                    (tableRow[8] as TextView).text = RadioParametersUtils.getCellId(radioParametersDto)

                    if (neighbors_table_layout.childCount > 1 && neighbors_table_layout.childCount > index + 1)
                        neighbors_table_layout.removeViewAt(index + 1)
                    neighbors_table_layout.addView(tableRow, index + 1)

                    if (neighbors_table_layout.childCount > it.size)
                        neighbors_table_layout.removeViews(it.size - 1, neighbors_table_layout.childCount - it.size)
                }
        }
    }

    private fun checkIfLayoutsAreAvailable():Boolean = this.isResumed

    private fun decToHex(dec: Int): String? {
        return String.format("%x", dec)
    }

    private fun hexToDec(hex: String): Int {
        return hex.toInt(16)
    }
    //</editor-fold>

}

