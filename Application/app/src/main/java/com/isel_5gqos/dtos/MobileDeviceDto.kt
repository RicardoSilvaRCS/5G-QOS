package com.isel_5gqos.dtos

import org.json.JSONObject

class MobileDeviceDto (
    val mobileUnitId : Int,
    val password : String,
    val controlConnectionHref : String,
    val systemLogHref : String
)
{
    companion object{

        fun jsonObjectToMobileDeviceDto(jsonObject: JSONObject): MobileDeviceDto {

            val probeId = jsonObject["probeId"].toString().toInt()
            val password = jsonObject["password"].toString()
            val controlConnectionHref = jsonObject["controlConnectionHref"].toString()
            val systemLogHref = jsonObject["systemLogHref"].toString()

            return MobileDeviceDto(probeId, password,controlConnectionHref ,systemLogHref)
        }

    }
}