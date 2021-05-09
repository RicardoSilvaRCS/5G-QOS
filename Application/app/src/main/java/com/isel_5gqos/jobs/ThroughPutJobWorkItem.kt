package com.isel_5gqos.jobs
/*

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.net.TrafficStats
import android.os.PersistableBundle
import android.util.Log
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.ThroughPut
import com.isel_5gqos.utils.errors.Exceptions
import java.util.*

class ThroughPutJobWorkItem : JobService() {

    private val context = QosApp.msWebApi.ctx
    private var jobCancelled = false;


    override fun onStartJob(params: JobParameters?): Boolean {

        fun work():Boolean {

            val sessionId = params?.extras?.getString(SESSION_ID).toString()

            do {

            } while (!jobCancelled)

            return true
        }

        asyncTask({work()}){}
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        jobCancelled = true;
        return true
    }

}


fun scheduleThroughPutJob (sessionId: String): JobInfo {

    val builder = JobInfo.Builder(0, ComponentName(QosApp.msWebApi.ctx,ThroughPutJobWorkItem::class.java))

    val extras = PersistableBundle(2)
    extras.putString(SESSION_ID,sessionId)

    val job = builder
        .setExtras(extras)
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        .build()

    QosApp.msWebApi.ctx.getSystemService(JobScheduler::class.java).schedule(job)
    return job
}
*/
