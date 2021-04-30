package com.isel_5gqos.dtos

import com.isel_5gqos.common.NetworkDataTypesEnum

class RadioParametersDto(
    val tech: String? = null,
    val arfcn: Int? = null,  //Absolute Radio-Frequency Channel Number
    val rssi: Int? = null,   //Received signal strength indication
    val rsrp: Int? = null,   //Reference signal receive power
    val cId: Int? = null,    //Carrier id GSM
    val psc: Int? = null,    //Primary scrambling controller UMTS
    val pci: Int? = null,    //Primary cell Identity LTE
    val rssnr: Int? = null,  //Reference Signal Signal-to-noise Ratio
    val rsrq: Int? = null,   //Reference Signal Received Quality
    val netDataType: NetworkDataTypesEnum,
    val isServingCell : Boolean
) {

    fun getCellId() = when(netDataType) {
        NetworkDataTypesEnum.LTE -> pci.toString()
        NetworkDataTypesEnum.GSM -> cId.toString()
        NetworkDataTypesEnum.UMTS -> psc.toString()
        else -> pci.toString()
    }
}

class LocationDto(
    val networkOperatorName:String,
    val latitude:Double,
    val longitude:Double
)

class WrapperDto(
    val radioParametersDtos: List<RadioParametersDto>,
    val servingCell: RadioParametersDto,
    val locationDto: LocationDto
){
    override fun toString(): String = "$locationDto\n$servingCell\n$radioParametersDtos"
}