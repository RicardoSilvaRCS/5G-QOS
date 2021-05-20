package com.isel_5gqos.jobs.works

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import com.isel_5gqos.QosApp
import com.isel_5gqos.QosApp.Companion.db
import com.isel_5gqos.common.TAG
import com.isel_5gqos.common.db.entities.Location
import com.isel_5gqos.common.db.entities.RadioParameters
import com.isel_5gqos.dtos.WrapperDto
import com.isel_5gqos.utils.errors.Exceptions
import com.isel_5gqos.utils.mobile_utils.LocationUtils
import com.isel_5gqos.utils.mobile_utils.MobileInfoUtils
import com.isel_5gqos.utils.mobile_utils.RadioParametersUtils
import java.util.*

class RadioParametersWork : IWorks {

    override fun work(params: Map<String, Any?>) {

        val telephonyManager: TelephonyManager = params["telephonyManager"] as TelephonyManager
        val sessionId: String = params["sessionId"] as String
        val context: Context = params["context"] as Context

        try {

            val cellInfoList = RadioParametersUtils.getRadioParameters(telephonyManager)

            insertRadioParametersInfoInDb(
                sessionId,
                WrapperDto(
                    radioParametersDtos = cellInfoList,
                    locationDto = LocationUtils.getLocation(telephonyManager, context)
                )
            )


        } catch (ex: Exception) {
            Exceptions(ex)
        }

    }

    private fun insertRadioParametersInfoInDb(sessionId: String, wrapperDto: WrapperDto) {

        db.radioParametersDao().invalidateRadioParameters(sessionId)

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
                rssnr = radioParametersDto.rssnr ?: -1,
                rsrq = radioParametersDto.rsrq ?: -1,
                netDataType = radioParametersDto.netDataType.toString(),
                isServingCell = radioParametersDto.isServingCell,
                sessionId = sessionId,
                timestamp = System.currentTimeMillis(),
                isUpToDate = true
            )
        }.toTypedArray()

        db.radioParametersDao().insert(*radioParams)

        try {

            val location = Location(
                regId = UUID.randomUUID().toString(),
                networkOperatorName = wrapperDto.locationDto.networkOperatorName!!,
                latitude = wrapperDto.locationDto.latitude!!,
                longitude = wrapperDto.locationDto.longitude!!,
                sessionId = sessionId,
                timestamp = System.currentTimeMillis(),
            )
            db.locationDao().insert(location)

        } catch (ex: Exceptions) {
            Exceptions(ex)
        }
    }

    override fun getWorkTimeout(): Long = 1000L

    override fun getWorkParameters(): Array<String> = arrayOf("telephonyManager", "sessionId", "context")

}