package com.isel_5gqos.dtos

import com.isel_5gqos.common.NetworkDataTypesEnum
import com.isel_5gqos.common.db.entities.RadioParameters

class RadioParametersDto(
    val no: Int = -1,
    val tech: String? = null,
    val arfcn: Int? = null,  //Absolute Radio-Frequency Channel Number
    val rssi: Int? = null,   //Received signal strength indication
    val rsrp: Int? = null,   //Reference signal receive power
    val cId: Int? = null,    //Carrier id GSM
    val psc: Int? = null,    //Primary scrambling controller UMTS
    val pci: Int? = null,    //Primary cell Identity LTE
    val rssnr: Int? = null,  //Reference Signal Signal-to-noise Ratio
    val rsrq: Int? = null,   //Reference Signal Received Quality
    val netDataType: NetworkDataTypesEnum = NetworkDataTypesEnum.LTE,
    val isServingCell: Boolean = false
) {

    companion object {
        fun convertRadioParametersToDto(radioParams: List<RadioParameters>) = radioParams.map { convertRadioParametersToDto(it) }

        fun convertRadioParametersToDto(radioParameter: RadioParameters) = RadioParametersDto(
            no = radioParameter.no,
            tech = radioParameter.tech,
            arfcn = radioParameter.arfcn,
            rssi = radioParameter.rssi,
            rsrp = radioParameter.rsrp,
            cId = radioParameter.cId,
            psc = radioParameter.psc,
            pci = radioParameter.pci,
            rssnr = radioParameter.rssnr,
            rsrq = radioParameter.rsrq,
            netDataType = NetworkDataTypesEnum.valueOf(radioParameter.netDataType.toUpperCase()),
            isServingCell = radioParameter.isServingCell,
        )

    }

    fun getCellId() = when (netDataType) {
        NetworkDataTypesEnum.LTE -> pci.toString()
        NetworkDataTypesEnum.GSM -> cId.toString()
        NetworkDataTypesEnum.UMTS -> psc.toString()
        else -> pci.toString()
    }
}

class LocationDto(
    val networkOperatorName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

class WrapperDto(
    var radioParametersDtos: List<RadioParametersDto>,
    var servingCell: RadioParametersDto = RadioParametersDto(),
    var locationDto: LocationDto
) {
    override fun toString(): String = "$locationDto\n$servingCell\n$radioParametersDtos"

    companion object {
        fun makeDefault() = WrapperDto(
            radioParametersDtos = mutableListOf(),
            servingCell = RadioParametersDto(),
            locationDto = LocationDto()
        )

        private fun getServingCell(cellInfoList: MutableList<RadioParametersDto>) =
            if (cellInfoList.isEmpty()) RadioParametersDto()
            else cellInfoList.find { it.isServingCell } ?: cellInfoList[0]
    }
}