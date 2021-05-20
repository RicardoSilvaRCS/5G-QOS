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

        private val bands = mapOf(
            Pair(0, 599) to "2100",
            Pair(600, 1199) to "1900",
            Pair(1200, 1949) to "1800",
            Pair(1950, 2399) to "1700",
            Pair(2400, 2649) to "850",
            Pair(2750, 3449) to "2600",
            Pair(3450, 3799) to "900",
            Pair(3800, 4149) to "1800",
            Pair(4150, 4749) to "1700",
            Pair(4750, 4949) to "1500",
            Pair(5010, 5179) to "700",
            Pair(5180, 5279) to "700",
            Pair(5280, 5729) to "700",
            Pair(5730, 5849) to "700",
            Pair(5850, 5999) to "850",
            Pair(6000, 6149) to "850",
            Pair(6150, 6449) to "800",
            Pair(6450, 6599) to "1500",
            Pair(6600, 7399) to "3500",
            Pair(7500, 7699) to "2000",
            Pair(7700, 8039) to "1600",
            Pair(8040, 9039) to "850",
            Pair(9040, 9209) to "850",
            Pair(9210, 9659) to "700",
            Pair(9660, 9769) to "700",
            Pair(9770, 9869) to "2300",
            Pair(9870, 9919) to "450",
            Pair(9920, 10359) to "1500",
            Pair(65536, 66435) to "2100",
            Pair(66436, 67335) to "1700",
            Pair(67336, 67535) to "700",
            Pair(67536, 67835) to "700",
            Pair(67836, 68335) to "2600",
            Pair(68336, 68538) to "1700",
            Pair(68586, 68935) to "600",
            Pair(25144, 256143) to "5200",
            Pair(261519, 262143) to "5800"
        )

        fun getLTEBand(arfcn: Int): String? {
            bands.entries.forEach {
                val (key, value) = it
                if (arfcn >= key.first && arfcn <= key.second)
                    return value
            }
            return null
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
                    tech = "L${getLTEBand(cellInfo.cellIdentity.earfcn)}",
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

