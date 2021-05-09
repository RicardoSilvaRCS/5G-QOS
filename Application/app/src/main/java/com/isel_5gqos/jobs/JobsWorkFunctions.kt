package com.isel_5gqos.jobs

import android.content.Context
import android.net.TrafficStats
import android.telephony.TelephonyManager
import android.util.Log
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.entities.RadioParameters
import com.isel_5gqos.common.db.entities.ThroughPut
import com.isel_5gqos.dtos.WrapperDto
import com.isel_5gqos.utils.errors.Exceptions
import com.isel_5gqos.utils.mobile_utils.LocationUtils
import com.isel_5gqos.utils.mobile_utils.MobileInfoUtils
import com.isel_5gqos.utils.mobile_utils.RadioParametersUtils
import java.util.*

class JobsWorkFunctions {

    companion object {
        private const val throughputJobTimeout = 2000L
        private const val radioParametersJobTimeout =  5000L

        fun createWorkerParams(workType: String, vararg paramPairs: Any?): Map<String, Any?> {
            val paramsMap = mutableMapOf<String, Any?>()
            paramPairs.forEachIndexed { index, param ->
                paramsMap[WorkTypes[workType]?.get(index)!!] = param
            }
            return paramsMap
        }

        fun radioParametersWork(params: Map<String, Any?>) {
            Log.v("jobType","RadioParams----------------------------------")
            val telephonyManager: TelephonyManager = params["telephonyManager"] as TelephonyManager
            val sessionId: String = params["sessionId"] as String
            val context: Context = params["context"] as Context
            try {

                val cellInfoList = RadioParametersUtils.getRadioParameters(telephonyManager)

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

//                Thread.sleep(radioParametersJobTimeout)

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

        fun throughputWork(params: Map<String, Any?>) {
            Log.v("jobType","Throughput")
            val sessionId: String = params["sessionId"] as String
            try {
                val oldCountRX = TrafficStats.getMobileRxBytes()
                val oldCountTX = TrafficStats.getMobileTxBytes()

                Thread.sleep(WorkTypes.timeouts[THROUGHPUT_TYPE]!!)

                val newCountRx = TrafficStats.getMobileRxBytes()
                val newCountTx = TrafficStats.getMobileTxBytes()

                val mobileTxPackets = TrafficStats.getMobileTxPackets()

                insertThroughputInfoInDb(
                    rxResult = (newCountTx - oldCountTX) * BITS_IN_BYTE / (K_BIT * (throughputJobTimeout / 1000).toDouble()).toLong(),
                    txResult = (newCountRx - oldCountRX) * BITS_IN_BYTE / (K_BIT * (throughputJobTimeout / 1000).toDouble()).toLong(),
                    sessionId = sessionId
                )

                Log.v(TAG, "RX = ${newCountRx - oldCountRX}")
                Log.v(TAG, "TX = ${newCountTx - oldCountTX}")

            } catch (ex: Exception) {

                Exceptions(ex)

            }
        }

        private fun insertThroughputInfoInDb(rxResult: Long, txResult: Long, sessionId: String) {

            val throughPut = ThroughPut(
                regId = UUID.randomUUID().toString(),
                txResult = txResult,
                rxResult = rxResult,
                sessionId = sessionId,
                timestamp = System.currentTimeMillis()
            )

            QosApp.db.throughPutDao().insert(throughPut)

        }
    }
}
