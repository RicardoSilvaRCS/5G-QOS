package com.isel_5gqos.workers

import android.content.Context
import androidx.work.*
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.*
import com.isel_5gqos.dtos.NavigationDto
import com.isel_5gqos.dtos.TestDto
import com.isel_5gqos.dtos.TestPlanDto
import com.isel_5gqos.dtos.TestPlanResultDto
import com.isel_5gqos.utils.DateUtils.Companion.getDateIso8601Format
import com.isel_5gqos.utils.mobile_utils.LocationUtils
import com.isel_5gqos.workers.work.WorkTypeEnum
import com.isel_5gqos.workers.work.WorksMap
import java.util.*


class AutonomousTestWorker (private val context: Context, private val workerParams: WorkerParameters) : Worker (context, workerParams)  {

    private val results : MutableList<Any> = mutableListOf()
    private lateinit var token : String
    private var deviceId : Int = -1
    private lateinit var testPlanId : String

    override fun doWork(): Result {

         token = inputData.getString(TOKEN).toString()
         deviceId = inputData.getInt(DEVICE_SERVICE_ID,-1)
         testPlanId = inputData.getString(TEST_PLAN_ID).toString()

        getTestPlan()

        return Result.success()
    }

    private fun getTestPlan() {

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

    private fun postResultsToApi ( result : TestPlanResultDto, onPostExec : () -> Unit) {
        //TODO Guardar Resultados na Db

        QosApp.msWebApi.postTestPlanResults (
            authenticationToken = token,
            deviceId = deviceId,
            testPlanResult = result,
            onSuccess = {

                onPostExec()

            },
            onError = {

                onPostExec()

            }
        )

    }

    private fun runWork (tests : MutableList<TestDto>) {

        if(tests.isEmpty()) {
            return
        }

        val currTest = tests.first()
        val testType = WorkTypeEnum.valueOf(currTest.testType.toUpperCase())
        val location = LocationUtils.getLocation(context)

        val resultDto = TestPlanResultDto(
            id = Random().nextInt(),
            date =  getDateIso8601Format(),
            navigationDto = NavigationDto(
               gpsFix = location?.provider ?: "",
               latitude = location?.latitude ?: 0.0,
               longitude = location?.longitude ?: 0.0,
               speed = location?.speed,
            ),
            probeId = deviceId,
            testId = currTest.id,
            testPlanId = testPlanId,
            type = currTest.testType
        )

        WorksMap.worksMap[testType]?.work(currTest,resultDto) {
            results.add(it)
            postResultsToApi(it){
                tests.removeFirst()
                runWork(tests)
            }
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