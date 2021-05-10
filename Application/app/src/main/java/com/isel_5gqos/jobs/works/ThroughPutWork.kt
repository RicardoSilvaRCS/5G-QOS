package com.isel_5gqos.jobs.works

import android.net.TrafficStats
import android.util.Log
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.entities.ThroughPut
import com.isel_5gqos.utils.errors.Exceptions
import java.util.*

class ThroughPutWork : IWorks {

    companion object {
        private const val throughputJobTimeout = 2000L
    }

    override fun work(params: Map<String, Any?>) {
        Log.v("jobType", "Throughput")
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

    override fun getWorkTimeout(): Long = 1000L

    override fun getWorkParameters(): Array<String> = arrayOf("sessionId")

}