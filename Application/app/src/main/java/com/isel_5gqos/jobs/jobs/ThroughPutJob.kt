package com.isel_5gqos.jobs.jobs

import android.content.Context
import android.net.TrafficStats
import android.telephony.TelephonyManager
import android.util.Log
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.entities.ThroughPut
import com.isel_5gqos.jobs.JobParametersEnum
import com.isel_5gqos.common.utils.errors.Exceptions
import com.isel_5gqos.common.utils.mobile_utils.LocationUtils
import java.util.*

class ThroughPutJob : IJob {

    companion object {
        private const val throughputJobTimeout = 2000L
    }

    override fun job(params: Map<JobParametersEnum, Any?>) {
        Log.v("jobType", "Throughput")

        val telephonyManager: TelephonyManager = params[JobParametersEnum.TelephonyManager] as TelephonyManager
        val sessionId: String = params[JobParametersEnum.SessionId] as String
        val context: Context = params[JobParametersEnum.Context] as Context

        try {
            val oldCountRX = TrafficStats.getMobileRxBytes()
            val oldCountTX = TrafficStats.getMobileTxBytes()

            Thread.sleep(getJobTimeout())

            val newCountRx = TrafficStats.getMobileRxBytes()
            val newCountTx = TrafficStats.getMobileTxBytes()

            //val mobileTxPackets = TrafficStats.getMobileTxPackets()

            val location = LocationUtils.getLocationDto(telephonyManager, context)

            insertThroughputInfoInDb(
                rxResult = (newCountTx - oldCountTX) * BITS_IN_BYTE / (K_BIT * (throughputJobTimeout / 1000).toDouble()).toLong(),
                txResult = (newCountRx - oldCountRX) * BITS_IN_BYTE / (K_BIT * (throughputJobTimeout / 1000).toDouble()).toLong(),
                longitude = if (location.longitude == null) "" else location.longitude.toString(),
                latitude = if(location.latitude == null) "" else location.longitude.toString(),
                sessionId = sessionId
            )

            Log.v(TAG, "RX = ${newCountRx - oldCountRX}")
            Log.v(TAG, "TX = ${newCountTx - oldCountTX}")

        } catch (ex: Exception) {

            Exceptions(ex)

        }
    }

    private fun insertThroughputInfoInDb(rxResult: Long, txResult: Long, longitude : String, latitude : String, sessionId: String) {

        val throughPut = ThroughPut(
            regId = UUID.randomUUID().toString(),
            txResult = txResult,
            rxResult = rxResult,
            longitude = longitude,
            latitude = latitude,
            sessionId = sessionId,
            timestamp = System.currentTimeMillis()
        )

        QosApp.db.throughPutDao().insert(throughPut)

    }

    override fun getJobTimeout(): Long = 1000L

    override fun getJobParameters(): Array<JobParametersEnum> = arrayOf(JobParametersEnum.TelephonyManager, JobParametersEnum.SessionId, JobParametersEnum.Context)

}