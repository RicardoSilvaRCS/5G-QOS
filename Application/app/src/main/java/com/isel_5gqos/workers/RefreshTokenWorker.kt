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
        val credentials = inputData.getString(CREDENTIALS).toString()
        val username  = inputData.getString(USER).toString()

        QosApp.msWebApi.refreshToken (
            authenticationToken = token,
            onSuccess = { refreshedToken ->

                asyncTask({

                    QosApp.db.userDao().updateToken(username,refreshedToken)

                }) {

                    notifyOnChannel(context.getString(R.string.token_notification_title)
                        , context.getString(R.string.token_refreshed_text)
                        ,SplashActivity::class.java
                        ,applicationContext)

                    scheduleRefreshTokenWorker(username,refreshedToken,credentials)
                }
            },
            onError = {
                val decodedCredentials = Base64.decode(credentials, Base64.DEFAULT)
                val (username,password) = String(decodedCredentials).split(":")
                QosApp.msWebApi.login(
                    username = username,
                    password = password,
                    onSuccess = {

                        asyncTask({

                            QosApp.db.userDao().updateToken(username,it.userToken)

                        }) {

                            notifyOnChannel(context.getString(R.string.token_notification_title)
                                , context.getString(R.string.token_refreshed_text)
                                ,SplashActivity::class.java
                                ,applicationContext)

                            scheduleRefreshTokenWorker(username,it.userToken,credentials)
                        }

                    },
                    onError = {
                        notifyOnChannel(context.getString(R.string.token_notification_title)
                            , context.getString(R.string.token_noti_text)
                            ,SplashActivity::class.java
                            ,applicationContext)
                    }
                )

            }
        )

        return Result.success()
    }

}

fun scheduleRefreshTokenWorker (username : String, token : String, credentials:String) {

    val inputData = workDataOf(TOKEN to token, CREDENTIALS to credentials)

    val request = OneTimeWorkRequestBuilder<RefreshTokenWorker>()
        .setInitialDelay(15,TimeUnit.MINUTES)
        .setInputData(inputData)
        .build()

    WorkManager.getInstance(QosApp.msWebApi.ctx).enqueueUniqueWork(WORKER_TAG, ExistingWorkPolicy.REPLACE ,request)
}