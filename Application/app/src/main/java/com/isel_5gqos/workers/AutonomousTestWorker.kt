package com.isel_5gqos.workers

import android.content.Context
import androidx.work.*
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.*
import java.util.concurrent.TimeUnit


class AutonomousTestWorker (private val context: Context, private val workerParams: WorkerParameters) : Worker (context, workerParams)  {

    override fun doWork(): Result {
        TODO("Not yet implemented")
    }

}

fun scheduleAutonomousTestWorker ( token : String ) {

    val inputData = workDataOf(TOKEN to token)

    val request = PeriodicWorkRequestBuilder<RefreshTokenWorker>(15,TimeUnit.MINUTES)
        //.setInitialDelay(15,TimeUnit.MINUTES)
        .setInputData(inputData)
        .build()

    WorkManager.getInstance(QosApp.msWebApi.ctx).enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, request)
}