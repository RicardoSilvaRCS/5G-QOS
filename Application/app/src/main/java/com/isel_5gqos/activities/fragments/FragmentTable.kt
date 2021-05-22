package com.isel_5gqos.activities.fragments

import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.isel_5gqos.R
import com.isel_5gqos.common.DEFAULT_SESSION_ID
import com.isel_5gqos.common.NetworkDataTypesEnum
import com.isel_5gqos.models.TestViewModel
import com.isel_5gqos.utils.mobile_utils.RadioParametersUtils
import kotlinx.android.synthetic.main.fragment_main_session.*
import kotlinx.android.synthetic.main.fragment_table.*


class FragmentTable : Fragment() {

    private val testModel by lazy {
        ViewModelProvider(requireActivity()).get(TestViewModel::class.java)
    }

    //<editor-fold desc="EVENTS">
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_table, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        distance_txt?.text = "---"
        registerObservers()

    }

    //</editor-fold>

    //<editor-fold desc="AUX FUNCTIONS"
    private fun registerObservers() {
        testModel.getServingCell(DEFAULT_SESSION_ID).observe(requireActivity()) {
            if(!checkIfLayoutsAreAvailable()) return@observe
            val servingCell = it.find { cell -> cell.isServingCell } ?: it.find { cell -> cell.no == 1 } ?: return@observe
            val telephonyManager = requireContext().getSystemService(TelephonyManager::class.java) as TelephonyManager
            val networkOperator = telephonyManager.networkOperator

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

        val layoutInflater = LayoutInflater.from(requireContext())
        testModel.registerRadioParametersChanges(DEFAULT_SESSION_ID).observe(requireActivity()) {
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

        testModel.getLastLocation(DEFAULT_SESSION_ID).observe(requireActivity()) {
            if (!checkIfLayoutsAreAvailable() || it == null) return@observe
            lat_lon_txt.text = "${it.latitude}/${it.longitude}"
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

