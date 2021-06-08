package com.isel_5gqos.workers

import android.content.Context
import android.util.Base64
import androidx.work.*
import com.isel_5gqos.QosApp
import com.isel_5gqos.R
import com.isel_5gqos.activities.SplashActivity
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.utils.android_utils.AndroidUtils.Companion.notifyOnChannel
import java.util.concurrent.TimeUnit

class RefreshTokenWorker(private val context: Context, private val workerParams: WorkerParameters) : Worker (context, workerParams) {


    override fun doWork(): Result {

        val token = inputData.getString(TOKEN).toString()
        val deviceId = inputData.getInt(DEVICE_SERVICE_ID,-1)
        val username  = inputData.getString(USER).toString()
        //        val credentials = inputData.getString(CREDENTIALS).toString()

        QosApp.msWebApi.refreshToken (
            authenticationToken = token,
            onSuccess = { refreshedToken ->

                asyncTask({

                    QosApp.db.loginDao().updateToken(username,refreshedToken)

                }) {

                    notifyOnChannel(context.getString(R.string.token_notification_title)
                        , context.getString(R.string.token_refreshed_text)
                        ,SplashActivity::class.java
                        ,applicationContext)

                    scheduleRefreshTokenWorker(username,refreshedToken,deviceId)
                }
            },
            onError = {

                notifyOnChannel(context.getString(R.string.token_notification_title)
                    , context.getString(R.string.token_noti_text)
                    ,SplashActivity::class.java
                    ,applicationContext)

            }
        )

        return Result.success()
    }

}

fun scheduleRefreshTokenWorker (username : String, token : String, mobileId : Int) {

    val inputData = workDataOf(USER to username, TOKEN to token, DEVICE_SERVICE_ID to mobileId)

    val request = OneTimeWorkRequestBuilder<RefreshTokenWorker>()
        .setInitialDelay(15,TimeUnit.MINUTES)
        .setInputData(inputData)
        .build()

    WorkManager.getInstance(QosApp.msWebApi.ctx).enqueueUniqueWork(WORKER_TAG, ExistingWorkPolicy.REPLACE ,request)
}