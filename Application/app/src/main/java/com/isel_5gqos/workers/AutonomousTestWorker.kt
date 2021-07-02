package com.isel_5gqos.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.google.gson.Gson
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.DEVICE_SERVICE_ID
import com.isel_5gqos.common.TEST_PLAN_STRING
import com.isel_5gqos.common.TOKEN_FOR_WORKER
import com.isel_5gqos.common.WORKER_TAG
import com.isel_5gqos.dtos.TestDto
import com.isel_5gqos.dtos.TestPlanDto
import com.isel_5gqos.dtos.TestPlanResultDto
import com.isel_5gqos.utils.DateUtils.Companion.getDateIso8601Format
import com.isel_5gqos.utils.android_utils.AndroidUtils
import com.isel_5gqos.utils.qos_utils.EventEnum
import com.isel_5gqos.utils.qos_utils.QoSUtils
import com.isel_5gqos.utils.qos_utils.QoSUtils.Companion.getProbeLocation
import com.isel_5gqos.utils.qos_utils.SystemLogProperties
import com.isel_5gqos.workers.work.WorkTypeEnum
import com.isel_5gqos.workers.work.WorksMap
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit


class AutonomousTestWorker(private val context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val results: MutableList<Any> = mutableListOf()
    private val token by lazy {
        AndroidUtils.getPreferences(TOKEN_FOR_WORKER, applicationContext)!!
    }
    private var deviceId: Int = -1
    private lateinit var testPlanStr: String

    override fun doWork(): Result {
//        token = AndroidUtils.getPreferences("TOKEN",context)!!
        deviceId = inputData.getInt(DEVICE_SERVICE_ID, -1)
        testPlanStr = inputData.getString(TEST_PLAN_STRING)!!

        getTestPlan()

        return Result.success()
    }

    private fun getTestPlan() {
        val testPlan = Gson().fromJson(testPlanStr, TestPlanDto::class.java)
        Log.v("TestTest", testPlan.name)
        QoSUtils.logToServer(
            token = token,
            deviceId = deviceId,
            event = EventEnum.TESTPLAN_STARTED,
            context = context,
            props = SystemLogProperties(
                testPlanId = testPlan.id
            )
        ) {

            /**Needs to be on "onPostExecution" to  avoid disturbing mobile network usage*/
            executeTestPlan(testPlan = testPlan)

        }
    }

    private fun executeTestPlan(testPlan: TestPlanDto) {
        if (!testPlan.tests.isNullOrEmpty()) {
            val tests = testPlan.tests.toMutableList()
            runWork(tests)
        }
    }

    private fun runWork(tests: MutableList<TestDto>) {

        if (tests.isEmpty()) {
            return
        }

        val currTest = tests.first()

        QoSUtils.logToServer(
            token = token,
            deviceId = deviceId,
            event = EventEnum.TEST_START,
            context = context,
            props = SystemLogProperties(
                testId = currTest.id,
            )
        ) {

            /**Needs to be on "onPostExecution" to  avoid disturbing mobile network usage*/
            val testType = WorkTypeEnum.valueOf(currTest.testType.toUpperCase())

            val resultDto = TestPlanResultDto(
                date = getDateIso8601Format(),
                navigationDto = getProbeLocation(context),
                probeId = deviceId,
                testId = currTest.id,
                testPlanId = testPlanStr,
                type = currTest.testType
            )

            try {

                WorksMap.worksMap[testType]?.work(currTest, resultDto) {
                    results.add(it)

                    postResultsToApi(it) {
                        QoSUtils.logToServer(
                            token = token,
                            deviceId = deviceId,
                            event = EventEnum.TEST_END,
                            context = context,
                            props = SystemLogProperties(
                                testId = currTest.id,
                            )
                        ) {
                            /**Needs to be on "onPostExecution" to  avoid disturbing mobile network usage*/
                            tests.removeFirst()
                            runWork(tests)
                        }
                    }
                }

            } catch (e: Exception) {

                /**If test execution throw's and exception will log to Management System*/
                QoSUtils.logToServer(
                    token = token,
                    deviceId = deviceId,
                    event = EventEnum.TEST_ERROR,
                    context = context,
                    props = SystemLogProperties(
                        testId = currTest.id,
                        cause = e.toString()
                    )
                ) {
                    /**Needs to be on "onPostExecution" to  avoid disturbing mobile network usage*/
                    tests.removeFirst()
                    runWork(tests)
                }
            }

        }

    }

    private fun postResultsToApi(result: TestPlanResultDto, onPostExec: () -> Unit) {
        QosApp.msWebApi.postTestPlanResults(
            authenticationToken = token,
            deviceId = deviceId,
            testPlanResult = result,
            onSuccess = {

                onPostExec()

            },
            onError = {

                /**Reporting test start*/
                QoSUtils.logToServer(
                    token = token,
                    deviceId = deviceId,
                    event = EventEnum.TEST_ERROR,
                    context = context,
                    props = SystemLogProperties(
                        testId = result.testId,
                        cause = it.cause.toString()
                    )
                ) {
                    onPostExec()
                }

            }
        )

    }

}

fun scheduleAutonomousTestWorker(deviceId: Int, testPlanDto: TestPlanDto, testPlanStringified: String) { //receber data

    val inputData = workDataOf(DEVICE_SERVICE_ID to deviceId, TEST_PLAN_STRING to testPlanStringified)

    val startDate = LocalDateTime.parse(testPlanDto.startDate).plusHours(1).toEpochSecond(ZoneOffset.UTC)
    val currentDate = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    Log.v("TestTest", "Current date: ${LocalDateTime.ofEpochSecond(currentDate,0, ZoneOffset.UTC)}, Start date: ${LocalDateTime.ofEpochSecond(startDate,0, ZoneOffset.UTC)} ${if (startDate > currentDate) startDate - currentDate else 0} seconds to test")
    val request = OneTimeWorkRequestBuilder<AutonomousTestWorker>()
        .setInputData(inputData)
        .setInitialDelay(if (startDate > currentDate) startDate - currentDate else 0, TimeUnit.SECONDS)
        .build()
    Log.v("TesTest", "Queue test ${testPlanDto.name} by ${testPlanDto.creator}")
    WorkManager.getInstance(QosApp.msWebApi.ctx).enqueueUniqueWork(WORKER_TAG, ExistingWorkPolicy.REPLACE, request)
}