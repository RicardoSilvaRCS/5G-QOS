package com.isel_5gqos.workers

import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.work.*
import com.google.gson.Gson
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.DEVICE_SERVICE_ID
import com.isel_5gqos.common.TEST_PLAN_STRING
import com.isel_5gqos.common.TOKEN_FOR_WORKER
import com.isel_5gqos.common.WORKER_TAG
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.TestPlanResult
import com.isel_5gqos.common.enums.TestPlanStatesEnum
import com.isel_5gqos.common.utils.DateUtils.Companion.getDateIso8601Format
import com.isel_5gqos.common.utils.android_utils.AndroidUtils
import com.isel_5gqos.common.utils.errors.Exceptions
import com.isel_5gqos.common.utils.qos_utils.EventEnum
import com.isel_5gqos.common.utils.qos_utils.QoSUtils
import com.isel_5gqos.common.utils.qos_utils.QoSUtils.Companion.getProbeLocation
import com.isel_5gqos.common.utils.qos_utils.SystemLogProperties
import com.isel_5gqos.dtos.TestDto
import com.isel_5gqos.dtos.TestPlanDto
import com.isel_5gqos.dtos.TestPlanResultDto
import com.isel_5gqos.workers.work.WorkTypeEnum
import com.isel_5gqos.workers.work.WorksMap
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.TimeUnit


class AutonomousTestWorker(private val context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val results: MutableList<Any> = mutableListOf()
    private val token by lazy {
        AndroidUtils.getPreferences(TOKEN_FOR_WORKER, applicationContext)!!
    }
    private var deviceId: Int = -1
    private lateinit var testPlan: TestPlanDto
    private val gson = Gson()

    override fun doWork(): Result {

        deviceId = inputData.getInt(DEVICE_SERVICE_ID, -1)
        testPlan = Gson().fromJson(inputData.getString(TEST_PLAN_STRING)!!, TestPlanDto::class.java)
        getTestPlan()
        Log.v("THREAD_ID", "SCHEDULE IsMain=${Looper.myLooper() == Looper.getMainLooper()} ThreadId=${Thread.currentThread().id} ${testPlan.name}")
        return Result.success()
    }

    private fun getTestPlan() {
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
            Log.v("THREAD_ID", "START IsMain=${Looper.myLooper() == Looper.getMainLooper()} ThreadId=${Thread.currentThread().id} ${testPlan.name}")

            asyncTask({ QosApp.db.testPlanDao().updateTestPLan(testPlan.id, TestPlanStatesEnum.STARTED.toString()) })
            /**Needs to be on "onPostExecution" to  avoid disturbing mobile network usage*/
            executeTestPlan()
        }
    }

    private fun executeTestPlan() {
        if (!testPlan.tests.isNullOrEmpty()) {
            val tests = testPlan.tests.toMutableList()
            runWork(tests)
        }
    }

    private fun runWork(tests: MutableList<TestDto>) {

        if (tests.isEmpty()) {
            QoSUtils.logToServer(
                token = token,
                deviceId = deviceId,
                event = EventEnum.TESTPLAN_FINISHED,
                context = context,
                props = SystemLogProperties(
                    testPlanId = testPlan.id
                )
            ){
                Log.v("CONTROL_C", "Jácabou Jéssica")
                Log.v("THREAD_ID", "FINISH IsMain=${Looper.myLooper() == Looper.getMainLooper()} ThreadId=${Thread.currentThread().id} ${testPlan.name}")
                val t = Thread {
                    QosApp.db.testPlanDao().updateTestPLan(testPlan.id, TestPlanStatesEnum.FINISHED.toString())
                }
                t.start()
                t.join()
            }

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
                testPlanId = testPlan.id,
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
                                testPlanId = testPlan.id,
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

                Exceptions(e)
            }

        }

    }

    private fun storeTestResult(testPlanResult: TestPlanResultDto, isReported: Boolean) {

        val testResult = TestPlanResult(
            regId = UUID.randomUUID().toString(),
            testPlanId = testPlanResult.testPlanId,
            testId = testPlanResult.testId,
            result = gson.toJson(testPlanResult),
            isReported = isReported,
            type = testPlanResult.type
        )
        Log.v("CONTROL_C", "Vou inserir, tudo a postos")

        val t = Thread {
//        asyncTask(
//            doInBackground = {
                Log.v(
                    "THREAD_ID",
                    "INSERT IsMain=${Looper.myLooper() == Looper.getMainLooper()} ThreadId=${Thread.currentThread().id} ${testPlan.name}"
                )
                QosApp.db.testPlanResultDao().insert(testResult)
//            }
//        )
//
        }
//
        t.start()
        t.join()
    }

    private fun postResultsToApi(result: TestPlanResultDto, onPostExec: () -> Unit) {
        QosApp.msWebApi.postTestPlanResults(
            authenticationToken = token,
            deviceId = deviceId,
            testPlanResult = result,
            onSuccess = {

                storeTestResult(result, true)
                onPostExec()

            },
            onError = {

                storeTestResult(result, false)

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
    Log.v(
        "TestTest",
        "Current date: ${LocalDateTime.ofEpochSecond(currentDate, 0, ZoneOffset.UTC)}, Start date: ${
            LocalDateTime.ofEpochSecond(
                startDate,
                0,
                ZoneOffset.UTC
            )
        } ${if (startDate > currentDate) startDate - currentDate else 0} seconds to test"
    )
    val request = OneTimeWorkRequestBuilder<AutonomousTestWorker>()
        .setInputData(inputData)
        .setInitialDelay(if (startDate > currentDate) startDate - currentDate else 0, TimeUnit.SECONDS)
        .build()
    Log.v("TesTest", "Queue test ${testPlanDto.name} by ${testPlanDto.creator}")
    WorkManager.getInstance(QosApp.msWebApi.ctx).enqueueUniqueWork(WORKER_TAG, ExistingWorkPolicy.REPLACE, request)
}