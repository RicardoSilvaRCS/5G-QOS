package com.isel_5gqos.workers

import android.content.Context
import android.net.TrafficStats
import android.util.Log
import androidx.work.*
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.*
import com.isel_5gqos.QosApp.Companion.db
import com.isel_5gqos.common.db.entities.ThroughPut
import com.isel_5gqos.utils.Errors.Exceptions
import java.util.*

//This is only possible because this code is being executed in background on a worker thread from the
// thread pool. When using the main thread, the insert must be done with an AsyncTask
class ThroughPutWorker(private val context: Context, private val workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    companion object {
        var notificationTimer = 1L
    }

    override suspend fun doWork(): Result {
        val workInfo = WorkManager.getInstance(context).getWorkInfoById(this.id)
        val sessionId = inputData.getString(SESSION_ID).toString()

        do {
            val oldCountRX = TrafficStats.getMobileRxBytes()
            val oldCountTX = TrafficStats.getMobileTxBytes()

            Thread.sleep(1000)
            if(workInfo.isCancelled) return Result.success()

            val newCountRx = TrafficStats.getMobileRxBytes()
            val newCountTx = TrafficStats.getMobileTxBytes()

            val mobileTxPackets = TrafficStats.getMobileTxPackets()

            try {
                val throughPut = ThroughPut(
                    regId = UUID.randomUUID().toString(),
                    txResult = (newCountTx - oldCountTX) * BITS_IN_BYTE / K_BIT,
                    rxResult = (newCountRx - oldCountRX) * BITS_IN_BYTE / K_BIT,
                    sessionId = sessionId,
                    timestamp = System.currentTimeMillis()
                )

                db.throughPutDao().insert(throughPut)

            } catch (ex: Exception) {

                Exceptions(ex)

            }

        } while (!workInfo.isCancelled)
        return Result.success()
    }
}

fun scheduleThroughPutBackgroundWork(sessionId: String): OneTimeWorkRequest {
    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    val inputData = workDataOf(SESSION_ID to sessionId)

    val request: OneTimeWorkRequest = OneTimeWorkRequest.Builder(ThroughPutWorker::class.java)
        .setConstraints(constraints)
        .setInputData(inputData)
        .build()

    WorkManager.getInstance(QosApp.msWebApi.ctx).enqueueUniqueWork(WORKER_TAG,ExistingWorkPolicy.REPLACE,request)

    return request
}

//TODO ("implement observer in activity")