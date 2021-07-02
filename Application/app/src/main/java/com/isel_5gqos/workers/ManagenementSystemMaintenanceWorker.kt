package com.isel_5gqos.workers

import android.content.Context
import androidx.work.*
import com.android.volley.VolleyError
import com.isel_5gqos.QosApp
import com.isel_5gqos.R
import com.isel_5gqos.activities.SplashActivity
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.utils.android_utils.AndroidUtils
import com.isel_5gqos.utils.android_utils.AndroidUtils.Companion.notifyOnChannel
import com.isel_5gqos.utils.qos_utils.EventEnum
import com.isel_5gqos.utils.qos_utils.QoSUtils
import com.isel_5gqos.utils.qos_utils.SystemLogProperties
import org.json.JSONArray
import java.util.concurrent.TimeUnit

class ManagenementSystemMaintenanceWorker(private val context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {


    override fun doWork(): Result {

        val token = AndroidUtils.getPreferences(TOKEN_FOR_WORKER, applicationContext)!!
        val deviceId = inputData.getInt(DEVICE_SERVICE_ID, -1)
        val username = inputData.getString(USER).toString()

        QosApp.msWebApi.refreshToken(
            authenticationToken = token,
            onSuccess = { refreshedToken ->
                AndroidUtils.setPreferences(TOKEN_FOR_WORKER, refreshedToken, applicationContext)
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

                        requestControlConnection(
                            deviceId = deviceId,
                            token = token,
                            onSuccess = {
                                for (i in 0 until it.length()) {
                                    getTestPlan(token, deviceId, it[i] as String)
                                }
                            },
                            onError = {
                                QoSUtils.logToServer(
                                    token = token,
                                    deviceId = deviceId,
                                    event = EventEnum.CONTROL_CONNECTION_ERROR,
                                    context = context,
                                    props = SystemLogProperties(
                                        cause = it.cause.toString()
                                    )
                                )
                            }
                        )

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

    private fun requestControlConnection(deviceId: Int, token: String, onSuccess: (JSONArray) -> Unit, onError: (VolleyError) -> Unit) {
        //TODO: Falta obter o ControlConnectionDto
        QosApp.msWebApi.controlConnection(
            deviceId = deviceId,
            navigationDto = QoSUtils.getProbeLocation(applicationContext),// ControlConnectionDto("FIX_3D", 30, 40, 100, 38.687918099, -9.316346627, 55),
            onSuccess = onSuccess,
            onError = onError,
            authenticationToken = token
        )
    }

    private fun getTestPlan(token: String, deviceId: Int, testPlanId: String) {
        QosApp.msWebApi.getTestPlan(
            authenticationToken = token,
            deviceId = deviceId,
            testPlanId = testPlanId,
            onSuccess = { testPlan, testPlanStr ->

                /**Reporting test start*/
                QoSUtils.logToServer(
                    token = token,
                    deviceId = deviceId,
                    event = EventEnum.TESTPLAN_SCHEDULED,
                    context = context,
                    props = SystemLogProperties(
                        testPlanId = testPlanId
                    )
                ) {

                    /**Needs to be on "onPostExecution" to  avoid disturbing mobile network usage*/
                    //Schedule work
//                    executeTestPlan(testPlan = testPlan)
                    scheduleAutonomousTestWorker(deviceId, testPlan, testPlanStr)
                }
            },
            onError = {

                /**Reporting get test plan error*/
                QoSUtils.logToServer(
                    token = token,
                    deviceId = deviceId,
                    event = EventEnum.TESTPLAN_ERROR,
                    context = context,
                    props = SystemLogProperties(
                        testPlanId = testPlanId,
                        cause = it.cause.toString()
                    )
                ) {}
            }
        )
    }
}

fun scheduleRefreshTokenWorker(username: String, mobileId: Int) {

    val inputData = workDataOf(USER to username, DEVICE_SERVICE_ID to mobileId)

    val request = PeriodicWorkRequestBuilder<ManagenementSystemMaintenanceWorker>(15, TimeUnit.MINUTES)
        .setInputData(inputData)
        .build()

    WorkManager.getInstance(QosApp.msWebApi.ctx).enqueueUniquePeriodicWork(REFRESH_WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, request)
}