package com.isel_5gqos.dtos

import com.isel_5gqos.common.NetworkDataTypesEnum
import com.isel_5gqos.common.db.entities.RadioParameters

class RadioParametersDto(
    val no: Int = -1,
    val tech: String? = null,
    val arfcn: Int? = null,  //Absolute Radio-Frequency Channel Number
    val rssi: Int? = null,   //Received signal strength indication ===> GSM
    val rsrp: Int? = null,   //Reference signal receive power ===> LTE
    val cId: Int? = null,    //Carrier id GSM
    val psc: Int? = null,    //Primary scrambling controller UMTS
    val pci: Int? = null,    //Primary cell Identity LTE
    val rssnr: Int? = null,  //Reference Signal Signal-to-noise Ratio
    val rsrq: Int? = null,   //Reference Signal Received Quality
    var latitude : String = "",   //Location of the values
    var longitude : String = "",  //Location of the values
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
            latitude = radioParameter.latitude,
            longitude =  radioParameter.longitude,
            netDataType = NetworkDataTypesEnum.valueOf(radioParameter.netDataType.toUpperCase()),
            isServingCell = radioParameter.isServingCell,
        )

        fun getServingCell(radioParams: List<RadioParametersDto>) : RadioParametersDto =
            if (radioParams.isEmpty()) RadioParametersDto()
            else radioParams.find { it.isServingCell } ?: radioParams.find { it.no == 1 } ?: RadioParametersDto()
    }

    override fun toString(): String = "No = $no, tech = $tech, arfcn = $arfcn, rssi = $rssi, rsrp = $rsrp, cId = $cId, psc = $psc, pci = $pci, rssnr = $rssnr, rsrq = $rsrq"


}

class LocationDto(
    val networkOperatorName : String,
    val latitude : Double?,
    val longitude : Double?
)