package com.isel_5gqos.jobs
/*
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

class RadioParametersJobWorkItem : JobService() {

    private val context = QosApp.msWebApi.ctx
    private var jobCancelled = false;
    private val telephonyManager = ContextCompat.getSystemService(context, TelephonyManager::class.java)
    private lateinit var allParamsMap:Map<String,Any?>
    private val functionsMap = mapOf(
        RADIO_PARAMS_TYPE to JobsWorkFunctions::radioParametersWork
    )

    override fun onStartJob(params: JobParameters?): Boolean {

        fun work(): Boolean {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false
            }

            val dequeueWork = params!!.dequeueWork() as JobWorkItem
            val stringExtra = dequeueWork.intent.getStringExtra("args") //stringExtra

            val saveToDb = params?.extras?.getBoolean(DB_SAVE) ?: false
            val sessionId = if (saveToDb) params?.extras?.getString(SESSION_ID).toString() else "-1"

            allParamsMap = mapOf(
                "telephonyManager" to telephonyManager,
                "sessionId" to sessionId,
                "context" to context
            )

            val paramsList: Array<Any?> = WorkTypes[stringExtra?:""]!!.map { allParamsMap[it] }.toTypedArray()

            do {
                functionsMap[stringExtra!!]?.let { it(JobsWorkFunctions.createWorkerParams(stringExtra,*paramsList)) }

            } while (!jobCancelled)

            Log.v(TAG, "Finished work ascvacnwegdbujoscv adckhijoascjvschjkl dfbvgshdcklsddjbfvh aefjlk jhaskfdyjbdvg")

            return true
        }
        asyncTask({ work() }) {}
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.v("schedulerTest","Scheduler Stopped")
        jobCancelled = true;
        return true
    }

}


*/
/*
fun scheduleRadioParametersJob(sessionId: String, saveToDb: Boolean): JobInfo {

    val builder = JobInfo.Builder(0, ComponentName(QosApp.msWebApi.ctx, RadioParametersJobWorkItem::class.java))

    val extras = PersistableBundle(2)
    extras.putString(SESSION_ID, sessionId)
    extras.putBoolean(DB_SAVE, saveToDb)

    val job = builder
        .setExtras(extras)
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        .build()

//    QosApp.msWebApi.ctx.getSystemService(JobScheduler::class.java).schedule(job)
    val intent = Intent()
    intent.putExtra("args", RADIO_PARAMS_TYPE)
    QosApp.msWebApi.ctx.getSystemService(JobScheduler::class.java).enqueue(job, JobWorkItem(intent))

    return job
}*/

