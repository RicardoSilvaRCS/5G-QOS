package com.isel_5gqos.activities.fragments

import android.os.Build
import android.os.Bundle
import android.telephony.CellIdentityLte
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.TelephonyManager
import android.util.EventLog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.isel_5gqos.R
import com.isel_5gqos.common.DEFAULT_SESSION_ID
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
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this);
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

            val latitude = if(servingCell.latitude.isEmpty()) "----" else servingCell.latitude.subSequence(0,7)
            val longitude = if(servingCell.longitude.isEmpty()) "----" else servingCell.longitude.subSequence(0,7)

            lat_lon_txt.text = "${latitude}/${longitude}"

            serving_cell_tech.text = servingCell.tech

            val netDataType = NetworkDataTypesEnum.valueOf(servingCell.netDataType.toUpperCase())
            if(netDataType == NetworkDataTypesEnum.GSM){
                view_switcher.nextView
                cid_txt.text = servingCell.cId.toString()
                rsrp_txt.text = "${servingCell.rssi} dBm"
                layout_rsrq.isGone = true
                layout_arfcn.isGone = true
            } else {
                rsrp_txt.text = "${servingCell.rsrp} dBm"
            }

            arfcn_txt.text = servingCell.arfcn.toString()
            rsrq_txt.text = "${ servingCell.rsrq } dB"
            rssnr_txt.text = "${servingCell.rssnr} dB"
            net_data_type_txt.text = netDataType.toString()
            mcc_txt.text = "${networkOperator.substring(0, 3)} ${networkOperator.substring(3)}"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tac_txt.text = telephonyManager.typeAllocationCode
            }

            pci_txt.text = servingCell.pci.toString()

            if(netDataType == NetworkDataTypesEnum.LTE){
                val cellIdentity = (telephonyManager.allCellInfo[0] as CellInfoLte).cellIdentity.ci
                e_node_b_txt.text = ((cellIdentity - servingCell.pci).shr(8)).toString()
            }

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

    private fun decToHex(dec: Int): String = String.format("%x", dec)

    private fun hexToDec(hex: String): Int = if(hex.isNotEmpty()) hex.toInt(16) else "0".toInt(16)
    //</editor-fold>

}

