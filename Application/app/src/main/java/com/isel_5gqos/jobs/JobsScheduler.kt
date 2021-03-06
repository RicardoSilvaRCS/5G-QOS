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
import com.isel_5gqos.jobs.jobs.IJob

class JobWorksScheduler : JobService() {

    private val context = QosApp.msWebApi.ctx
    private var jobCancelled = false;
    private val telephonyManager = ContextCompat.getSystemService(context, TelephonyManager::class.java)
    private lateinit var allParamsMap: Map<JobParametersEnum, Any?>

    override fun onStartJob(params: JobParameters?): Boolean {

        fun job(): Boolean {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false
            }

            val sessionId = params!!.extras.getString(SESSION_ID).toString()
            val dequeueWork = params.dequeueWork() as JobWorkItem
            val jobsList = dequeueWork.intent.getStringArrayListExtra(JOB_TYPE) ?: arrayListOf()

            allParamsMap = mapOf(
                JobParametersEnum.TelephonyManager to telephonyManager,
                JobParametersEnum.SessionId to sessionId,
                JobParametersEnum.Context  to context
            )

            val lastRuns = jobsList.map { 0L }.toMutableList()

            do {
                jobsList.forEachIndexed { index, jobType ->

                    val workInstance = JobsMap.worksMap[jobType]

                    if (System.currentTimeMillis() > (lastRuns[index] + workInstance!!.getJobTimeout())) {
                        lastRuns[index] = System.currentTimeMillis()

                        asyncTask({

                            workInstance.job(createWorkerParams(workInstance))

                        })

                    }
                }
            } while (!jobCancelled)

            Log.v(TAG, "Job Finished!! The red brown fox jumps over the lazy dog!!!!")

            return true
        }

        Thread{job()}.start()

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        jobCancelled = true;
        return true
    }

    private fun createWorkerParams(work: IJob): Map<JobParametersEnum, Any?> = work.getJobParameters().map {
        it to allParamsMap[it]
    }.toMap()

}

fun scheduleJob(sessionId: String, jobTypes: ArrayList<String> = arrayListOf()): JobInfo {
    val builder = JobInfo.Builder(0, ComponentName(QosApp.msWebApi.ctx, JobWorksScheduler::class.java))

    val extras = PersistableBundle(2)
    extras.putString(SESSION_ID, sessionId)

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
