package com.isel_5gqos.workers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.isel_5gqos.QosApp
import com.isel_5gqos.activities.SplashActivity
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.utils.android_utils.AndroidUtils
import com.isel_5gqos.utils.android_utils.AndroidUtils.Companion.notifyOnChannel
import java.util.concurrent.TimeUnit

class RefreshTokenWorker(private val context: Context, private val workerParams: WorkerParameters) : Worker (context, workerParams) {


    override fun doWork(): Result {

        val token = inputData.getString(TOKEN).toString()

        QosApp.msWebApi.refreshToken (
            authenticationToken = token,
            onSuccess = { refreshedToken ->

                asyncTask({

                    QosApp.db.userDao().updateToken(token,refreshedToken)

                }) {}

            },
            onError = {
                notifyOnChannel(context.getString(com.isel_5gqos.R.string.token_notification_title)
                                , context.getString(com.isel_5gqos.R.string.token_noti_text)
                                ,SplashActivity::class.java
                                ,applicationContext)
            }
        )

        return Result.success()
    }

}

fun scheduleRefreshTokenWorker ( token : String ) {

    val inputData = workDataOf(TOKEN to token)

    val request = PeriodicWorkRequestBuilder<RefreshTokenWorker>(15,TimeUnit.MINUTES)
        //.setInitialDelay(15,TimeUnit.MINUTES)
        .setInputData(inputData)
        .build()

    WorkManager.getInstance(QosApp.msWebApi.ctx).enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, request)
}