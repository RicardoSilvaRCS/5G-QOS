package com.isel_5gqos.utils.MobileUtils

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import com.isel_5gqos.common.MIN_RSSI
import com.isel_5gqos.common.NetworkDataTypesEnum
import com.isel_5gqos.dtos.RadioParametersDto


class RadioParametersUtils {

    companion object{

        @SuppressLint("MissingPermission")
        fun getRadioParameters (telephonyManager: TelephonyManager) : List<RadioParametersDto> {

            val cellInfoList: MutableList<RadioParametersDto> = mutableListOf()

            telephonyManager?.allCellInfo?.forEachIndexed { index, cellInfo ->
                val currentCell = convertCellInfoToRadioParameter(index, cellInfo)
                if (currentCell != null) {
                    cellInfoList.add(currentCell)
                }
            }

            return cellInfoList
        }

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
                    rssnr = cellInfo.cellSignalStrength.rssnr,
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
                    netDataType = NetworkDataTypesEnum.FiveG,
                    isServingCell = cellInfo.cellConnectionStatus == CellInfo.CONNECTION_PRIMARY_SERVING
                )
            }

            return null
        }

    }
}
