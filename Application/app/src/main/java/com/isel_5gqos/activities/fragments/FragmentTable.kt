package com.isel_5gqos.activities.fragments

import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.isel_5gqos.R
import com.isel_5gqos.common.DEFAULT_SESSION_ID
import com.isel_5gqos.models.TestViewModel
import kotlinx.android.synthetic.main.fragment_table.*


class FragmentTable : Fragment() {

    private val testModel by lazy {
        ViewModelProvider(requireActivity()).get(TestViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_table, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        distance_txt.text = "---"
        testModel.getServingCell(DEFAULT_SESSION_ID).observe(requireActivity()) {
            val servingCell = it.find { cell -> cell.isServingCell } ?: it.find { cell -> cell.no == 1 } ?: return@observe
            val telephonyManager = requireContext().getSystemService(TelephonyManager::class.java) as TelephonyManager
            val networkOperator = telephonyManager.networkOperator
            serving_cell_tech.text = servingCell.tech
            rsrp_txt.text = servingCell.rsrp.toString()
            arfcn_txt.text = servingCell.arfcn.toString()
            rsrq_txt.text = servingCell.rsrq.toString()
            rssnr_txt.text = servingCell.rssnr.toString()
            net_data_type_txt.text = servingCell.netDataType
            mcc_txt.text = "${networkOperator.substring(0, 3)} ${networkOperator.substring(3)}"
            tac_txt.text = telephonyManager.typeAllocationCode
            val cellidHex: String = decToHex(servingCell.cId) ?: ""
            val eNBHex = cellidHex.substring(0, cellidHex.length - 2)
            val eNB: Int = hexToDec(eNBHex)

            e_node_b_txt.text = eNB.toString()
            pci_txt.text = servingCell.pci.toString()

            sim_operator_txt.text = "${telephonyManager.simOperatorName}/${telephonyManager.networkOperatorName}"
        }
        testModel.getLastLocation(DEFAULT_SESSION_ID).observe(requireActivity()){
            if(it == null) return@observe
            lat_lon_txt.text = "${it.latitude}/${it.longitude}"
        }
    }
    private fun decToHex(dec: Int): String? {
        return String.format("%x", dec)
    }

    private fun hexToDec(hex: String): Int {
        return hex.toInt(16)
    }
}

