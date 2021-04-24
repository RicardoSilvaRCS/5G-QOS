package com.isel_5gqos.Workers

import android.content.Context
import android.net.TrafficStats
import android.util.Log
import androidx.work.*
import androidx.work.PeriodicWorkRequest
import com.isel_5gqos.Common.QoSApp
import com.isel_5gqos.Common.TAG
import java.lang.Exception
import java.util.concurrent.TimeUnit

class ThroughPutWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

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

                Log.v(TAG,"${newCountRx-oldCountRX} Rx Bytes")
                Log.v(TAG,"${newCountTx - oldCountTX} Tx Bytes")

            }catch (ex : Exception) {

                Log.v(TAG,ex.toString())

                return Result.failure()
            }

        }while (true)
    }
}

fun scheduleThroughPutBackgroundWork () {
    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    val request = PeriodicWorkRequest.Builder(ThroughPutWorker::class.java,ThroughPutWorker.notificationTimer,TimeUnit.SECONDS)
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(QoSApp.msWebApi.ctx).enqueue(request)
}