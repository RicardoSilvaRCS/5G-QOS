package com.isel_5gqos.workers

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.work.*
import com.isel_5gqos.QosApp
import com.isel_5gqos.R
import com.isel_5gqos.activities.SplashActivity
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.dtos.TestDto
import com.isel_5gqos.dtos.TestPlanDto
import com.isel_5gqos.dtos.TestResultDto
import com.isel_5gqos.utils.android_utils.AndroidUtils
import com.isel_5gqos.workers.work.WorkTypeEnum
import com.isel_5gqos.workers.work.WorksMap
import java.util.concurrent.TimeUnit


class AutonomousTestWorker (private val context: Context, private val workerParams: WorkerParameters) : Worker (context, workerParams)  {

    private val results : MutableList<TestResultDto> = mutableListOf()

    override fun doWork(): Result {

        val token = inputData.getString(TOKEN).toString()
        val deviceId = inputData.getInt(DEVICE_SERVICE_ID,-1)
        val testPlanId = inputData.getString(TEST_PLAN_ID).toString()

        getTestPlan(token,deviceId, testPlanId)

        return Result.success()
    }


    private fun getTestPlan(token : String, deviceId: Int, testPlanId: String) {

        QosApp.msWebApi.getTestPlan (
            authenticationToken = token,
            deviceId = deviceId,
            testPlanId = testPlanId,
            onSuccess = { testPlan ->

                executeTestPlan(testPlan = testPlan)

            },
            onError = {}
        )

    }

    private fun executeTestPlan (testPlan : TestPlanDto) {
        if (!testPlan.tests.isNullOrEmpty()){
            val tests = testPlan.tests.toMutableList()
            runWork(tests)
        }
    }

    private fun postResultsToApi () {
        //guardar na db
        //enviar para o sistema
    }

    private fun runWork (tests : MutableList<TestDto>) {

        if(tests.isEmpty()) {
            postResultsToApi()
            return
        }

        val currTest = tests.first()
        val testType = WorkTypeEnum.valueOf(currTest.testType.toUpperCase())

        WorksMap.worksMap[testType]?.work(currTest) {
            results.add(it)
            tests.removeFirst()
            runWork(tests)
        }

    }

}

fun scheduleAutonomousTestWorker ( token : String, deviceId : Int, testPlanId : String ) {

    val inputData = workDataOf(TOKEN to token, DEVICE_SERVICE_ID to deviceId, TEST_PLAN_ID to testPlanId)

    val request = OneTimeWorkRequestBuilder<AutonomousTestWorker>()
        .setInputData(inputData)
        .build()

    WorkManager.getInstance(QosApp.msWebApi.ctx).enqueueUniqueWork(WORKER_TAG, ExistingWorkPolicy.REPLACE ,request)
}