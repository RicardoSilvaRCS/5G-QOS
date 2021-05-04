package com.isel_5gqos.jobs

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.net.TrafficStats
import android.os.PersistableBundle
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.ThroughPut
import com.isel_5gqos.utils.errors.Exceptions
import java.util.*

class ThroughPutJobWorkItem : JobService() {

    private val context = QosApp.msWebApi.ctx
    private var jobCancelled = false;

    override fun onStartJob(params: JobParameters?): Boolean {

        fun work():Boolean {

            val sessionId = params?.extras?.getString(SESSION_ID).toString()

            do {
                try {
                    val oldCountRX = TrafficStats.getMobileRxBytes()
                    val oldCountTX = TrafficStats.getMobileTxBytes()

                    Thread.sleep(1000)

                    val newCountRx = TrafficStats.getMobileRxBytes()
                    val newCountTx = TrafficStats.getMobileTxBytes()

                    val mobileTxPackets = TrafficStats.getMobileTxPackets()

                    insertInfoInDb(
                        rxResult = (newCountTx - oldCountTX) * BITS_IN_BYTE / K_BIT,
                        txResult = (newCountRx - oldCountRX) * BITS_IN_BYTE / K_BIT,
                        sessionId = sessionId
                    )

                } catch (ex: Exception) {

                    Exceptions(ex)

                }

            } while (!jobCancelled)

            return true
        }

        asyncTask({work()}){}
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        jobCancelled = true;
        return true
    }

    private fun insertInfoInDb(rxResult : Long , txResult : Long, sessionId: String) {

        val throughPut = ThroughPut(
            regId = UUID.randomUUID().toString(),
            txResult = txResult,
            rxResult = rxResult,
            sessionId = sessionId,
            timestamp = System.currentTimeMillis()
        )

        asyncTask({QosApp.db.throughPutDao().insert(throughPut)}){}

    }

}


fun scheduleThroughPutJob (sessionId: String): JobInfo {

    val builder = JobInfo.Builder(0, ComponentName(QosApp.msWebApi.ctx,RadioParametersJobWorkItem::class.java))

    val extras = PersistableBundle(2)
    extras.putString(SESSION_ID,sessionId)

    val job = builder
        .setExtras(extras)
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        .build()

    QosApp.msWebApi.ctx.getSystemService(JobScheduler::class.java).schedule(job)
    return job
}
