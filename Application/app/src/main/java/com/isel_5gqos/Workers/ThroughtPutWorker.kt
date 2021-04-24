package com.isel_5gqos.Workers

import android.content.Context
import android.net.TrafficStats
import android.util.Log
import androidx.work.*
import com.isel_5gqos.Common.QoSApp
import com.isel_5gqos.Common.QoSApp.Companion.db
import com.isel_5gqos.Common.SESSION_ID
import com.isel_5gqos.Common.TAG
import com.isel_5gqos.dtos.ThroughPutDto
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.TimeUnit

class ThroughPutWorker(context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object{
        var notificationTimer = 1L
    }

    override fun doWork(): Result {
        do{
            val oldCountRX = TrafficStats.getMobileRxBytes()
            val oldCountTX = TrafficStats.getMobileTxBytes()
            Thread.sleep(1000)
            val newCountRx = TrafficStats.getMobileRxBytes()
            val newCountTx = TrafficStats.getMobileTxBytes()

            TrafficStats.getMobileTxPackets()

            //TODO: Go to DB
            try{
                val throughPut = ThroughPutDto(
                    regId = UUID.randomUUID().toString(),
                    txResult = newCountTx-oldCountRX,
                    rxResult = newCountRx-oldCountRX,
                    sessionId = inputData.getString(SESSION_ID).toString(),
                    timestamp = Timestamp(System.currentTimeMillis())
                )
                //This is only possible because this code is being executed in background on a worker thread from the
                // thread pool. When using the main thread, the insert must be done with an AsyncTask
                db.throughPutDao().insert(throughPut)

                Log.v(TAG,"${newCountRx-oldCountRX} Rx Bytes")
                Log.v(TAG,"${newCountTx - oldCountTX} Tx Bytes")

            }catch (ex : Exception) {

                Log.v(TAG,ex.toString())

                return Result.failure()
            }

        }while (true)
    }
}

fun scheduleThroughPutBackgroundWork (sessionId:String) {
    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    val inputData = workDataOf(SESSION_ID to sessionId)

    val request = OneTimeWorkRequest.Builder(ThroughPutWorker::class.java)
        .setConstraints(constraints)
        .setInputData(inputData)
        .build()

    WorkManager.getInstance(QoSApp.msWebApi.ctx).enqueue(request)
}