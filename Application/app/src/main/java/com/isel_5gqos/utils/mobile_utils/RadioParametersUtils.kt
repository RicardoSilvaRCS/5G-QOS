package com.isel_5gqos.utils.mobile_utils

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import com.isel_5gqos.common.MIN_RSSI
import com.isel_5gqos.common.NetworkDataTypesEnum
import com.isel_5gqos.common.db.entities.RadioParameters
import com.isel_5gqos.dtos.RadioParametersDto


class RadioParametersUtils {

    companion object {

        @SuppressLint("MissingPermission")
        fun getRadioParameters(telephonyManager: TelephonyManager): List<RadioParametersDto> {

            val cellInfoList: MutableList<RadioParametersDto> = mutableListOf()

            telephonyManager.allCellInfo?.forEachIndexed { index, cellInfo ->
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
                    tech = "G900",
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
                    tech = "U${cellInfo.cellSignalStrength}",
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

        fun getCellId(radioParameter: Any): String {

            var auxId = -1

            if (radioParameter is RadioParameters) {

                 when (convertStringToNetworkDataType(radioParameter.netDataType)) {
                    NetworkDataTypesEnum.LTE -> auxId = radioParameter.pci
                    NetworkDataTypesEnum.GSM -> auxId = radioParameter.cId
                    NetworkDataTypesEnum.UMTS -> auxId = radioParameter.psc
                    else -> auxId = radioParameter.pci
                }

            }

            if (radioParameter is RadioParametersDto) {

                when (radioParameter.netDataType) {
                    NetworkDataTypesEnum.LTE -> auxId = radioParameter.pci!!
                    NetworkDataTypesEnum.GSM -> auxId = radioParameter.cId!!
                    NetworkDataTypesEnum.UMTS -> auxId = radioParameter.psc!!
                    else -> auxId = radioParameter.pci!!
                }

            }

            return if(auxId == Int.MAX_VALUE || auxId == Int.MIN_VALUE) return "" else auxId.toString()
        }

        fun getReferenceStrength(radioParameter: Any): Int? {

            if (radioParameter is RadioParameters) {

                return when (convertStringToNetworkDataType(radioParameter.netDataType)) {
                    NetworkDataTypesEnum.LTE -> radioParameter.rsrp
                    NetworkDataTypesEnum.GSM -> radioParameter.rssi
                    else -> radioParameter.rssi
                }

            }

            if (radioParameter is RadioParametersDto) {

                return when (radioParameter.netDataType) {
                    NetworkDataTypesEnum.LTE -> radioParameter.rsrp
                    NetworkDataTypesEnum.GSM -> radioParameter.rssi
                    else -> radioParameter.rssi
                }

            }

            return null
        }

        fun convertStringToNetworkDataType(dataType: String): NetworkDataTypesEnum {
            return NetworkDataTypesEnum.valueOf(dataType.toUpperCase())
        }
    }
}

