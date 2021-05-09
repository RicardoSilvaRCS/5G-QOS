package com.isel_5gqos.jobs

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.TAG
import com.isel_5gqos.common.db.entities.RadioParameters
import com.isel_5gqos.dtos.WrapperDto
import com.isel_5gqos.utils.errors.Exceptions
import com.isel_5gqos.utils.mobile_utils.LocationUtils
import com.isel_5gqos.utils.mobile_utils.MobileInfoUtils
import com.isel_5gqos.utils.mobile_utils.RadioParametersUtils
import java.util.*

class JobsWorkFunctions {
    companion object {
        fun radioParametersWork(telephonyManager: TelephonyManager,sessionId:String,context: Context) {
            try {

                val cellInfoList = RadioParametersUtils.getRadioParameters(telephonyManager!!)

                val imei = MobileInfoUtils.getImei(context, telephonyManager)

                //TODO CAN get this info only once
                Log.v(TAG, "${telephonyManager.networkOperatorName} Network Operator name")
                Log.v(TAG, "${telephonyManager.networkOperator} MCC/MNC")
                Log.v(TAG, "${imei ?: ""} IMEI")

                insertRadioParametersInfoInDb(
                    sessionId,
                    WrapperDto(
                        radioParametersDtos = cellInfoList,
                        locationDto = LocationUtils.getLocation(telephonyManager, context)
                    )
                )

                Thread.sleep(5000)

            } catch (ex: Exception) {
                Exceptions(ex)
            }
        }
        private fun insertRadioParametersInfoInDb(sessionId: String, wrapperDto: WrapperDto) {

            QosApp.db.radioParametersDao().invalidateRadioParameters(sessionId)

            val radioParams = wrapperDto.radioParametersDtos.map { radioParametersDto ->

                RadioParameters(
                    regId = UUID.randomUUID().toString(),
                    no = radioParametersDto.no,
                    tech = radioParametersDto.tech ?: "",
                    arfcn = radioParametersDto.arfcn ?: -1,
                    rssi = radioParametersDto.rssi ?: -1,
                    rsrp = radioParametersDto.rsrp ?: -1,
                    cId = radioParametersDto.cId ?: -1,
                    psc = radioParametersDto.psc ?: -1,
                    pci = radioParametersDto.pci ?: -1,
                    rssnr = radioParametersDto.pci ?: -1,
                    rsrq = radioParametersDto.pci ?: -1,
                    netDataType = radioParametersDto.netDataType.toString(),
                    isServingCell = radioParametersDto.isServingCell,
                    sessionId = sessionId,
                    timestamp = System.currentTimeMillis(),
                    isUpToDate = true
                )
            }.toTypedArray()

            QosApp.db.radioParametersDao().insert(*radioParams)

//        val location = Location(
//            regId = UUID.randomUUID().toString(),
//            networkOperatorName = wrapperDto.locationDto.networkOperatorName!!,
//            latitude = wrapperDto.locationDto.latitude!!,
//            longitude = wrapperDto.locationDto.longitude!!,
//            sessionId = sessionId,
//            timestamp = System.currentTimeMillis(),
//        )
//
//        db.locationDao().insert(location)
        }
    }
}