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
import com.isel_5gqos.QosApp.Companion.db
import com.isel_5gqos.common.DB_SAVE
import com.isel_5gqos.common.SESSION_ID
import com.isel_5gqos.common.TAG
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.Location
import com.isel_5gqos.common.db.entities.RadioParameters
import com.isel_5gqos.dtos.WrapperDto
import com.isel_5gqos.utils.errors.Exceptions
import com.isel_5gqos.utils.mobile_utils.LocationUtils
import com.isel_5gqos.utils.mobile_utils.MobileInfoUtils
import com.isel_5gqos.utils.mobile_utils.RadioParametersUtils
import java.util.*

class RadioParametersJobWorkItem : JobService() {

    private val context = QosApp.msWebApi.ctx
    private var jobCancelled = false;
    private val functionsMap = mapOf(
        "radioParameters" to JobsWorkFunctions::radioParametersWork
    )
    override fun onStartJob(params: JobParameters?): Boolean {

        fun work(): Boolean {
            val dequeueWork = params!!.dequeueWork() as JobWorkItem
            val stringExtra = dequeueWork.intent.getStringExtra("args") //stringExtra

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false
            }

            val saveToDb = params?.extras?.getBoolean(DB_SAVE) ?: false
            val sessionId = if (saveToDb) params?.extras?.getString(SESSION_ID).toString() else "-1"

            val telephonyManager = ContextCompat.getSystemService(context, TelephonyManager::class.java)

            do {
                functionsMap[stringExtra!!]?.let { it(telephonyManager!!,sessionId,context) }
            } while (!jobCancelled)

            Log.v(TAG, "Finished work ascvacnwegdbujoscv adckhijoascjvschjkl dfbvgshdcklsddjbfvh aefjlk jhaskfdyjbdvg")

            return true
        }
        asyncTask({ work() }) {}
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        jobCancelled = true;
        return true
    }

}


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
    intent.putExtra("args","radioParameters")
    QosApp.msWebApi.ctx.getSystemService(JobScheduler::class.java).enqueue(job, JobWorkItem(intent))

    return job
}