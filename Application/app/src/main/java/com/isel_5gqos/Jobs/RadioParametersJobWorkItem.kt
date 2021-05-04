package com.isel_5gqos.Jobs

import android.Manifest
import android.app.job.*
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.PersistableBundle
import android.telephony.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.isel_5gqos.QosApp
import com.isel_5gqos.QosApp.Companion.db
import com.isel_5gqos.common.*
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

    override fun onStartJob(params: JobParameters?): Boolean {

        fun work(): Boolean {

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false
            }

            val saveToDb = params?.extras?.getBoolean(DB_SAVE) ?: false
            val sessionId = if (saveToDb) params?.extras?.getString(SESSION_ID).toString() else "-1"

            val telephonyManager = ContextCompat.getSystemService(context, TelephonyManager::class.java)

            do {
                try {

                    val cellInfoList = RadioParametersUtils.getRadioParameters(telephonyManager!!)

                    val imei = MobileInfoUtils.getImei(context, telephonyManager)

                    //TODO CAN get this info only once
                    Log.v(TAG, "${telephonyManager.networkOperatorName} Network Operator name")
                    Log.v(TAG, "${telephonyManager.networkOperator} MCC/MNC")
                    Log.v(TAG, "${imei ?: ""} IMEI")


                    insertInfoInDb(
                        sessionId,
                        WrapperDto(
                            radioParametersDtos = cellInfoList,
                            locationDto = LocationUtils.getLocation(telephonyManager, context)
                        )
                    )

                    Thread.sleep(10000)

                } catch (ex: Exception) {
                    Exceptions(ex)
                }

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

    private fun insertInfoInDb(sessionId: String, wrapperDto: WrapperDto) {

        db.radioParametersDao().invalidateRadioParameters(sessionId)

        val radioParams = wrapperDto.radioParametersDtos.map { radioParametersDto ->

            RadioParameters(
                regId = UUID.randomUUID().toString(),
                no = radioParametersDto.no,
                tech = radioParametersDto.tech ?: "",
                arfcn = radioParametersDto.arfcn ?: -1,
                rssi = radioParametersDto.rssi ?: -1,
                rsrp = radioParametersDto.rsrp ?: -1,
                cId = radioParametersDto.cId ?: -1,
                psc = radioParametersDto.psc ?: -1,
                pci = radioParametersDto.pci ?: -1,
                rssnr = radioParametersDto.pci ?: -1,
                rsrq = radioParametersDto.pci ?: -1,
                netDataType = radioParametersDto.netDataType.toString(),
                isServingCell = radioParametersDto.isServingCell,
                sessionId = sessionId,
                timestamp = System.currentTimeMillis(),
                isUpToDate = true
            )
        }.toTypedArray()

        db.radioParametersDao().insert(*radioParams)

        val location = Location(
            regId = UUID.randomUUID().toString(),
            networkOperatorName = wrapperDto.locationDto.networkOperatorName!!,
            latitude = wrapperDto.locationDto.latitude!!,
            longitude = wrapperDto.locationDto.longitude!!,
            sessionId = sessionId,
            timestamp = System.currentTimeMillis(),
        )

        asyncTask({ db.locationDao().insert(location) }) {}

    }
}


fun scheduleRadioParametersJob(sessionId: String, saveToDb: Boolean): JobInfo {

    val builder = JobInfo.Builder(0, ComponentName(QosApp.msWebApi.ctx, RadioParametersJobWorkItem::class.java))

    val extras = PersistableBundle(2)
    extras.putString(SESSION_ID, sessionId)
    extras.putBoolean(DB_SAVE, saveToDb)

    val job = builder.setExtras(extras)
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        .build()

    QosApp.msWebApi.ctx.getSystemService(JobScheduler::class.java).schedule(job)
    return job
}