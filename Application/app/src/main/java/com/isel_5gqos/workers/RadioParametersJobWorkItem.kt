package com.isel_5gqos.workers

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.job.*
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.PersistableBundle
import android.provider.Settings
import android.telephony.*
import android.util.Log
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.RadioParameters
import com.isel_5gqos.dtos.LocationDto
import com.isel_5gqos.dtos.RadioParametersDto
import com.isel_5gqos.dtos.WrapperDto
import com.isel_5gqos.utils.Errors.Exceptions
import java.util.*

class RadioParametersJobWorkItem : JobService() {
    private val context = QosApp.msWebApi.ctx
    private var jobCancelled = false;
    override fun onStartJob(params: JobParameters?): Boolean {
        fun work():Boolean {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false
            }

//        val workInfo = WorkManager.getInstance(context).getWorkInfoById(this.id)
        val saveToDb = params?.extras?.getBoolean(DB_SAVE) ?: false
        val sessionId = if (saveToDb) params?.extras?.getString(SESSION_ID).toString() else "-1"

        val telephonyManager = ContextCompat.getSystemService(context, TelephonyManager::class.java)
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        do {
            try {
                val cellInfoList: MutableList<RadioParametersDto> = mutableListOf()

                telephonyManager!!.allCellInfo.forEachIndexed { index, cellInfo ->
                    val currentCell = convertCellInfoToRadioParameter(index, cellInfo)
                    if (currentCell != null) {
                        cellInfoList.add(currentCell)
                    }
                }

                //Network Operator Info
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkOperatorName = telephonyManager.networkOperatorName

                val imei = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Settings.Secure.getString(
                        context.getContentResolver(),
                        Settings.Secure.ANDROID_ID
                    )
                } else {
                    telephonyManager.imei
                }

                Log.v(TAG, "${telephonyManager.networkOperatorName} Network Operator name")
                Log.v(TAG, "${telephonyManager.networkOperator} MCC/MNC")
                Log.v(TAG, "${imei ?: ""} IMEI")

                asyncTask({
                    insertInfoInDb(
                        sessionId,
                        WrapperDto(
                            radioParametersDtos = cellInfoList,
                            /*locationDto = LocationDto(
                                  networkOperatorName = networkOperatorName,
                                  latitude = 1.0,//latLon.first,
                                  longitude = 1.0//latLon.second
                              )*/
                            locationDto = getLocation(telephonyManager)
                        )
                    )
                }) {}

                Thread.sleep(1000)

            } catch (ex: Exception) {
                Exceptions(ex)
            }
        } while (!jobCancelled)

        Log.v(TAG, "Finished work ascvacnwegdbujoscv adckhijoascjvschjkl dfbvgshdcklsddjbfvh aefjlk jhaskfdyjbdvg")

        return true
    }
        asyncTask({work()}){}
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        jobCancelled = true;
        return true
    }


    private fun convertCellInfoToRadioParameter(index: Int, cellInfo: CellInfo): RadioParametersDto? {
        if (cellInfo is CellInfoGsm) {
            return RadioParametersDto(
                no = index + 1,
                tech = "G${cellInfo.cellIdentity}",
                arfcn = cellInfo.cellIdentity.arfcn,
                rssi = if (cellInfo.cellConnectionStatus == CellInfo.CONNECTION_UNKNOWN || cellInfo.cellConnectionStatus == CellInfo.CONNECTION_NONE) MIN_RSSI else null,
                cId = cellInfo.cellIdentity.cid,
                netDataType = NetworkDataTypesEnum.GSM,
                isServingCell = cellInfo.cellConnectionStatus == CellInfo.CONNECTION_PRIMARY_SERVING
            )
        } else if (cellInfo is CellInfoLte) {
            return RadioParametersDto(
                no = index + 1,
                tech = "L${cellInfo.cellIdentity.bandwidth}",
                arfcn = cellInfo.cellIdentity.earfcn,
                rssi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) cellInfo.cellSignalStrength.rssi else MIN_RSSI,
                rsrp = cellInfo.cellSignalStrength.rsrp,
                pci = cellInfo.cellIdentity.pci,
                rssnr = cellInfo.cellSignalStrength.rssnr,
                rsrq = cellInfo.cellSignalStrength.rsrq,
                netDataType = NetworkDataTypesEnum.LTE,
                isServingCell = cellInfo.cellConnectionStatus == CellInfo.CONNECTION_PRIMARY_SERVING
            )
        } else if (cellInfo is CellInfoWcdma) {
            //Measure UMTS
            return RadioParametersDto(
                no = index + 1,
                tech = "U${cellInfo.cellIdentity}",
                arfcn = cellInfo.cellIdentity.uarfcn,
                rssi = if (cellInfo.cellConnectionStatus == CellInfo.CONNECTION_UNKNOWN || cellInfo.cellConnectionStatus == CellInfo.CONNECTION_NONE) MIN_RSSI else null,
                psc = cellInfo.cellIdentity.psc,
                netDataType = NetworkDataTypesEnum.UMTS,
                isServingCell = cellInfo.cellConnectionStatus == CellInfo.CONNECTION_PRIMARY_SERVING
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo is CellInfoNr) {
            //Measure 5G
            return RadioParametersDto(
                no = index + 1,
                tech = "5G${cellInfo.cellIdentity}",
                rssi = if (cellInfo.cellConnectionStatus == CellInfo.CONNECTION_UNKNOWN || cellInfo.cellConnectionStatus == CellInfo.CONNECTION_NONE) MIN_RSSI else null,
                netDataType = NetworkDataTypesEnum.FiveG,
                isServingCell = cellInfo.cellConnectionStatus == CellInfo.CONNECTION_PRIMARY_SERVING
            )
        }

        return null
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(telephonyManager: TelephonyManager): LocationDto {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        return LocationDto(
            networkOperatorName = telephonyManager.networkOperatorName,
            latitude = lastKnownLocation?.latitude,
            longitude = lastKnownLocation?.longitude
        )
        /* locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0f, object : LocationListener {

             override fun onLocationChanged(location: android.location.Location?) {
                 latLon = Pair(location?.latitude ?: 0.0, location?.longitude ?: 0.0)
             }

             override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
             override fun onProviderEnabled(provider: String?) {}
             override fun onProviderDisabled(provider: String?) {}
         })

         return LocationDto(
              networkOperatorName = telephonyManager.networkOperatorName,
              latitude = latLon.first,
              longitude = latLon.second
         )*/
    }

    private fun insertInfoInDb(sessionId: String, wrapperDto: WrapperDto) {
        wrapperDto.radioParametersDtos.forEach { radioParametersDto ->
            val radioParameter = RadioParameters(
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

            asyncTask({ QosApp.db.radioParametersDao().invalidateRadioParameters(radioParameter.sessionId) }) {
                asyncTask({ QosApp.db.radioParametersDao().insert(radioParameter)}) {}
            }
        }

//        val location = Location(
//             regId= UUID.randomUUID().toString(),
//             networkOperatorName= wrapperDto.locationDto.networkOperatorName,
//             latitude= wrapperDto.locationDto.latitude,
//             longitude= wrapperDto.locationDto.longitude,
//             sessionId= sessionId,
//             timestamp= System.currentTimeMillis(),
//        )
//
//        db.locationDao().insert(location)

    }
}


fun scheduleRadioParametersJob(sessionId: String,saveToDb:Boolean):JobInfo {
    val builder = JobInfo.Builder(0, ComponentName(QosApp.msWebApi.ctx,RadioParametersJobWorkItem::class.java))
    val extras = PersistableBundle(2)
    extras.putString(SESSION_ID,sessionId)
    extras.putBoolean(DB_SAVE, saveToDb)
    builder.setExtras(extras)
    val job = builder.build()
    QosApp.msWebApi.ctx.getSystemService(JobScheduler::class.java).schedule(job)
    return job
}