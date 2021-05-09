package com.isel_5gqos.jobs

import android.Manifest
import android.app.job.*
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.PersistableBundle
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask

class JobsWorkItem : JobService() {

    private val context = QosApp.msWebApi.ctx
    private var jobCancelled = false;
    private val telephonyManager = ContextCompat.getSystemService(context, TelephonyManager::class.java)
    private lateinit var allParamsMap:Map<String,Any?>
    private val jobsMap = mapOf(
        RADIO_PARAMS_TYPE to JobsWorkFunctions::radioParametersWork,
        THROUGHPUT_TYPE to JobsWorkFunctions::throughputWork
    )

    override fun onStartJob(params: JobParameters?): Boolean {
        fun work(): Boolean {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false
            }

            val saveToDb = params!!.extras.getBoolean(DB_SAVE) ?: false
            val sessionId = if (saveToDb) params.extras.getString(SESSION_ID).toString() else "-1"
            val dequeueWork = params.dequeueWork() as JobWorkItem
            val jobsList = dequeueWork.intent.getStringArrayListExtra(JOB_TYPE)?: arrayListOf()

            allParamsMap = mapOf(
                "telephonyManager" to telephonyManager,
                "sessionId" to sessionId,
                "context" to context
            )

            val lastRuns = jobsList.map { 0L }.toMutableList()

            do {
                jobsList.forEachIndexed { index, jobType ->
                    val nextAvailableTime = lastRuns[index] + WorkTypes.timeouts[jobType]!!
                    if(System.currentTimeMillis() > nextAvailableTime){
                        lastRuns[index] = System.currentTimeMillis()

                        val paramsList: Array<Any?> = WorkTypes[jobType]!!.map { allParamsMap[it] }.toTypedArray()
                        asyncTask({jobsMap[jobType]?.let { it(JobsWorkFunctions.createWorkerParams(jobType,*paramsList)) }}){}
                    }
                }
            } while (!jobCancelled)

            Log.v(TAG, "Finished work ascvacnwegdbujoscv adckhijoascjvschjkl dfbvgshdcklsddjbfvh aefjlk jhaskfdyjbdvg")

            return true
        }
        Thread{work()}.start()
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        jobCancelled = true;
        return true
    }
}

fun scheduleJob(sessionId: String, saveToDb: Boolean, jobTypes:ArrayList<String> = arrayListOf()): JobInfo{
        val builder = JobInfo.Builder(0, ComponentName(QosApp.msWebApi.ctx, JobsWorkItem::class.java))

        val extras = PersistableBundle(2)
        extras.putString(SESSION_ID, sessionId)
        extras.putBoolean(DB_SAVE, saveToDb)

        val job = builder
            .setExtras(extras)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .build()

//    QosApp.msWebApi.ctx.getSystemService(JobScheduler::class.java).schedule(job)
        val intent = Intent()
        intent.putStringArrayListExtra(JOB_TYPE, jobTypes)
        QosApp.msWebApi.ctx.getSystemService(JobScheduler::class.java).enqueue(job, JobWorkItem(intent))

       return job
    }
/*
fun scheduleJob(sessionId: String, saveToDb: Boolean, jobTypes:ArrayList<String>): JobInfo {

    val builder = JobInfo.Builder(WorkTypes.typeJobIds[jobTypes]!!, ComponentName(QosApp.msWebApi.ctx, JobsWorkItem::class.java))

    val extras = PersistableBundle(2)
    extras.putString(SESSION_ID, sessionId)
    extras.putBoolean(DB_SAVE, saveToDb)

    val job = builder
        .setExtras(extras)
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        .build()

//    QosApp.msWebApi.ctx.getSystemService(JobScheduler::class.java).schedule(job)
    val intent = Intent()
    intent.putStringArrayListExtra(JOB_TYPE, jobTypes)

    QosApp.msWebApi.ctx.getSystemService(JobScheduler::class.java).enqueue(job, JobWorkItem(intent))

    return job
}*/
