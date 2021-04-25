package com.isel_5gqos.workers

import android.content.Context
import android.net.TrafficStats
import android.util.Log
import androidx.work.*
import com.isel_5gqos.common.*
import com.isel_5gqos.common.QoSApp.Companion.db
import com.isel_5gqos.common.db.entities.ThroughPut
import com.isel_5gqos.models.QosViewModel
import java.util.*

class ThroughPutWorker(private val context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object {
        var notificationTimer = 1L
    }

    override fun doWork(): Result {
        val workInfo = WorkManager.getInstance(context).getWorkInfoById(this.id)
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
                    sessionId = inputData.getString(SESSION_ID).toString(),
                    timestamp = System.currentTimeMillis()
                )

                //This is only possible because this code is being executed in background on a worker thread from the
                // thread pool. When using the main thread, the insert must be done with an AsyncTask
                db.throughPutDao().insert(throughPut)
                Log.v(TAG,"${throughPut.rxResult} TX KBits/s , ${throughPut.txResult} tx KBits/s WORKER WORKER WORKER")
            } catch (ex: Exception) {

                Log.v(TAG, ex.toString())

                return Result.failure()
            }

        } while (!workInfo.isCancelled)
        return Result.success()
    }
}

fun scheduleThroughPutBackgroundWork(sessionId: String) {
    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    val inputData = workDataOf(SESSION_ID to sessionId)

    val request: OneTimeWorkRequest = OneTimeWorkRequest.Builder(ThroughPutWorker::class.java)
        .setConstraints(constraints)
        .setInputData(inputData)
        .build()

    Log.v(TAG,request.id.toString())
    WorkManager.getInstance(QoSApp.msWebApi.ctx).enqueueUniqueWork(WORKER_TAG,ExistingWorkPolicy.REPLACE,request)
}