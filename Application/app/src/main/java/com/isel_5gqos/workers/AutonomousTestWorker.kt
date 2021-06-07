package com.isel_5gqos.workers

import android.content.Context
import androidx.work.*
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.*
import com.isel_5gqos.dtos.TestDto
import com.isel_5gqos.dtos.TestPlanDto
import com.isel_5gqos.dtos.TestResultDto
import com.isel_5gqos.workers.work.WorkTypeEnum
import com.isel_5gqos.workers.work.WorksMap
import java.util.concurrent.TimeUnit


class AutonomousTestWorker (private val context: Context, private val workerParams: WorkerParameters) : Worker (context, workerParams)  {

    private val results : MutableList<TestResultDto> = mutableListOf() // passar para TestResult

    override fun doWork(): Result {

        //Vai buscar o plano de testes
        val testPlan = getTestPlan()

        if (testPlan != null && !testPlan.tests.isNullOrEmpty()){

            val tests = testPlan.tests.toMutableList()
            runWork(tests)
        }

        return Result.success()
    }


    private fun getTestPlan () : TestPlanDto? {
        return null
    }

    private fun postResultsToApi () {

    }

    private fun runWork (tests : MutableList<TestDto>) {

        if(tests.isEmpty()) postResultsToApi()

        val currTest = tests.first()
        val testType = WorkTypeEnum.valueOf(currTest.testType.toUpperCase())


        WorksMap.worksMap[testType]?.work(currTest) {
            results.add(it)
            tests.removeFirst()
            runWork(tests)
        }

    }

}

fun scheduleAutonomousTestWorker ( token : String, deviceId : String, testPlanId : String ) {

    val inputData = workDataOf(TOKEN to token)

    val request = PeriodicWorkRequestBuilder<RefreshTokenWorker>(15,TimeUnit.MINUTES)
        //.setInitialDelay(15,TimeUnit.MINUTES)
        .setInputData(inputData)
        .build()

    WorkManager.getInstance(QosApp.msWebApi.ctx).enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, request)
}