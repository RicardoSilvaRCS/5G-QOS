package com.isel_5gqos.workers

import android.content.Context
import androidx.work.*
import com.isel_5gqos.QosApp
import com.isel_5gqos.R
import com.isel_5gqos.activities.SplashActivity
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.utils.android_utils.AndroidUtils.Companion.notifyOnChannel
import com.isel_5gqos.utils.qos_utils.EventEnum
import com.isel_5gqos.utils.qos_utils.QoSUtils
import com.isel_5gqos.utils.qos_utils.SystemLogProperties
import java.util.concurrent.TimeUnit

class RefreshTokenWorker(private val context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {


    override fun doWork(): Result {

        val token = inputData.getString(TOKEN).toString()
        val deviceId = inputData.getInt(DEVICE_SERVICE_ID, -1)
        val username = inputData.getString(USER).toString()

        QosApp.msWebApi.refreshToken(
            authenticationToken = token,
            onSuccess = { refreshedToken ->

                asyncTask({

                    QosApp.db.loginDao().updateToken(username, refreshedToken)

                }) {

                    /**Reporting test start*/
                    QoSUtils.logToServer(
                        token = token,
                        deviceId = deviceId,
                        event = EventEnum.CONTROL_CONNECTION_OK,
                        context = context,
                        props = SystemLogProperties()
                    ) {

                        notifyOnChannel(
                            context.getString(R.string.token_notification_title),
                            context.getString(R.string.token_refreshed_text),
                            SplashActivity::class.java,
                            applicationContext
                        )

                        scheduleRefreshTokenWorker(username, refreshedToken, deviceId)

                    }

                }
            },
            onError = {

                /**Reporting test start*/
                QoSUtils.logToServer(
                    token = token,
                    deviceId = deviceId,
                    event = EventEnum.CONTROL_CONNECTION_ERROR,
                    context = context,
                    props = SystemLogProperties(
                        cause = it.cause.toString()
                    )
                ) {

                    notifyOnChannel(
                        context.getString(R.string.token_notification_title),
                        context.getString(R.string.token_noti_text),
                        SplashActivity::class.java,
                        applicationContext
                    )

                }
            }

        )

        return Result.success()
    }

}

fun scheduleRefreshTokenWorker(username: String, token: String, mobileId: Int) {

    val inputData = workDataOf(USER to username, TOKEN to token, DEVICE_SERVICE_ID to mobileId)

    val request = OneTimeWorkRequestBuilder<RefreshTokenWorker>()
        .setInitialDelay(15, TimeUnit.MINUTES)
        .setInputData(inputData)
        .build()

    WorkManager.getInstance(QosApp.msWebApi.ctx).enqueueUniqueWork(WORKER_TAG, ExistingWorkPolicy.REPLACE, request)
}