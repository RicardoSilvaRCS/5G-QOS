package com.isel_5gqos.utils.mobile_utils

import android.os.Build
import android.telephony.*
import android.util.Log
import com.isel_5gqos.common.MIN_RSSI
import com.isel_5gqos.common.NetworkDataTypesEnum
import com.isel_5gqos.common.TAG
import com.isel_5gqos.dtos.RadioParametersDto


class RadioParametersPhoneStateListenerUtils() : PhoneStateListener() {

    var radioParametersDto = mutableListOf<RadioParametersDto>()

    override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
        Log.v(TAG,"Is Working")

        radioParametersDto.clear()

        cellInfo?.forEachIndexed { index, cell ->
            val currentCell = convertCellInfoToRadioParameter(index, cell)
            if (currentCell != null) {
                radioParametersDto.add(currentCell)
            }
        }

        super.onCellInfoChanged(cellInfo)
    }


    companion object {
        private fun convertCellInfoToRadioParameter(index: Int, cellInfo: CellInfo): RadioParametersDto? {
            if (cellInfo is CellInfoGsm) {
                return RadioParametersDto(
                    no = index + 1,
                    tech = "G${cellInfo.cellIdentity}",
                    arfcn = cellInfo.cellIdentity.arfcn,
                    rssi = if (cellInfo.cellConnectionStatus == CellInfo.CONNECTION_UNKNOWN || cellInfo.cellConnectionStatus == CellInfo.CONNECTION_NONE) MIN_RSSI else null,
                    cId = cellInfo.cellIdentity.cid,
                    netDataType = NetworkDataTypesEnum.GSM,
                    isServingCell = cellInfo.cellConnectionStatus == CellInfo.CONNECTION_PRIMARY_SERVING
                )
            } else if (cellInfo is CellInfoLte) {
                return RadioParametersDto(
                    no = index + 1,
                    tech = "L${cellInfo.cellIdentity.bandwidth}",
                    arfcn = cellInfo.cellIdentity.earfcn,
                    rssi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) cellInfo.cellSignalStrength.rssi else MIN_RSSI,
                    rsrp = cellInfo.cellSignalStrength.rsrp,
                    pci = cellInfo.cellIdentity.pci,
                    rssnr = (MIN_RSSI + cellInfo.cellSignalStrength.rsrq) / cellInfo.cellSignalStrength.rsrp,//cellInfo.cellSignalStrength.rssnr, //corrigir isto quando não é válido o gráfico fica muito grande
                    rsrq = cellInfo.cellSignalStrength.rsrq,
                    netDataType = NetworkDataTypesEnum.LTE,
                    isServingCell = cellInfo.cellConnectionStatus == CellInfo.CONNECTION_PRIMARY_SERVING
                )
            } else if (cellInfo is CellInfoWcdma) {
                //Measure UMTS
                return RadioParametersDto(
                    no = index + 1,
                    tech = "U${cellInfo.cellIdentity}",
                    arfcn = cellInfo.cellIdentity.uarfcn,
                    rssi = if (cellInfo.cellConnectionStatus == CellInfo.CONNECTION_UNKNOWN || cellInfo.cellConnectionStatus == CellInfo.CONNECTION_NONE) MIN_RSSI else null,
                    psc = cellInfo.cellIdentity.psc,
                    netDataType = NetworkDataTypesEnum.UMTS,
                    isServingCell = cellInfo.cellConnectionStatus == CellInfo.CONNECTION_PRIMARY_SERVING
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo is CellInfoNr) {
                //Measure 5G
                return RadioParametersDto(
                    no = index + 1,
                    tech = "5G${cellInfo.cellIdentity}",
                    rssi = if (cellInfo.cellConnectionStatus == CellInfo.CONNECTION_UNKNOWN || cellInfo.cellConnectionStatus == CellInfo.CONNECTION_NONE) MIN_RSSI else null,
                    netDataType = NetworkDataTypesEnum.FIVEG,
                    isServingCell = cellInfo.cellConnectionStatus == CellInfo.CONNECTION_PRIMARY_SERVING
                )
            }

            return null
        }
    }
}