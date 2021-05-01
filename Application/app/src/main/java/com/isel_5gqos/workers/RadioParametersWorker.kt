package com.isel_5gqos.workers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.telephony.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.*
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.entities.RadioParameters
import com.isel_5gqos.dtos.LocationDto
import com.isel_5gqos.dtos.RadioParametersDto
import com.isel_5gqos.dtos.WrapperDto
import java.util.*
import com.isel_5gqos.QosApp.Companion.db
import com.isel_5gqos.utils.Errors.Exceptions


class RadioParametersWorker(private val context: Context, private val workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {


    override suspend fun doWork(): Result {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Result.failure()
        }

        val workInfo = WorkManager.getInstance(context).getWorkInfoById(this.id)
        val sessionId = inputData.getString(SESSION_ID).toString()

        val telephonyManager = getSystemService(context, TelephonyManager::class.java)
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        do {
            try {

                val cellInfoList: MutableList<RadioParametersDto> = mutableListOf()


                telephonyManager!!.allCellInfo.forEachIndexed { index, cellInfo ->
                    val currentCell = convertCellInfoToRadioParameter(index,cellInfo)
                    if (currentCell != null) {
                        cellInfoList.add(currentCell)
                    }
                }

                val servingCell = getServingCell(cellInfoList)
                //Network Operator Info
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkOperatorName = telephonyManager.networkOperatorName


                Log.v(TAG, "${telephonyManager.networkOperatorName} Network Operator name")
                Log.v(TAG, "${telephonyManager.networkOperator} MCC/MNC")
                Log.v(TAG, "${telephonyManager.imei} IMEI")

                insertInfoInDb(
                    sessionId,
                    WrapperDto(
                        radioParametersDtos = cellInfoList,
                        servingCell = servingCell,
                        locationDto = LocationDto(
                            networkOperatorName = networkOperatorName,
                            latitude = 1.0,//latLon.first,
                            longitude = 1.0//latLon.second
                        )
                        //locationDto = getLocation(telephonyManager)
                    )
                )

                setProgress( workDataOf(PROGRESS to true) ) // notify main activity that model isn't up to date anymore

                Thread.sleep(10000)

            } catch (ex: Exception) {
                Exceptions(ex)
            }

        } while (!workInfo.isCancelled)

        return Result.success()
    }

    private fun getServingCell(cellInfoList: MutableList<RadioParametersDto>) =
        cellInfoList.find { it.isServingCell } ?: cellInfoList[0]

    private fun convertCellInfoToRadioParameter(index : Int , cellInfo: CellInfo): RadioParametersDto? {
        if (cellInfo is CellInfoGsm) {
            return RadioParametersDto(
                no= index + 1,
                tech = "G${cellInfo.cellIdentity}",
                arfcn = cellInfo.cellIdentity.arfcn,
                rssi = if (cellInfo.cellConnectionStatus == CellInfo.CONNECTION_UNKNOWN || cellInfo.cellConnectionStatus == CellInfo.CONNECTION_NONE) MIN_RSSI else null,
                cId = cellInfo.cellIdentity.cid,
                netDataType = NetworkDataTypesEnum.GSM,
                isServingCell = cellInfo.cellConnectionStatus == CellInfo.CONNECTION_PRIMARY_SERVING
            )
        } else if (cellInfo is CellInfoLte) {
            return RadioParametersDto(
                no= index + 1,
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
                no= index + 1,
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
                no= index + 1,
                tech = "5G${cellInfo.cellIdentity}",
                rssi = if (cellInfo.cellConnectionStatus == CellInfo.CONNECTION_UNKNOWN || cellInfo.cellConnectionStatus == CellInfo.CONNECTION_NONE) MIN_RSSI else null,
                netDataType = NetworkDataTypesEnum.FiveG,
                isServingCell = cellInfo.cellConnectionStatus == CellInfo.CONNECTION_PRIMARY_SERVING
            )
        }

        return null
    }

    @SuppressLint("MissingPermission")
    private fun getLocation (telephonyManager: TelephonyManager) : LocationDto {

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var latLon: Pair<Double, Double> = Pair(0.0, 0.0)

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0f, object : LocationListener {

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
        )
    }

    private fun insertInfoInDb(sessionId: String, wrapperDto: WrapperDto) {

        wrapperDto.radioParametersDtos.forEachIndexed { index, radioParametersDto ->
            val radioParameter = RadioParameters(
                regId = UUID.randomUUID().toString(),
                no = radioParametersDto.no,
                tech= radioParametersDto.tech ?: "",
                arfcn= radioParametersDto.arfcn ?: -1 ,
                rssi= radioParametersDto.rssi?: -1,
                rsrp= radioParametersDto.rsrp?: -1,
                cId= radioParametersDto.cId?: -1,
                psc= radioParametersDto.psc?: -1,
                pci= radioParametersDto.pci?: -1,
                rssnr= radioParametersDto.pci?: -1,
                rsrq= radioParametersDto.pci?: -1,
                netDataType= radioParametersDto.netDataType.toString(),
                isServingCell = radioParametersDto.isServingCell,
                sessionId= sessionId,
                timestamp= System.currentTimeMillis(),
            )

            db.radioParametersDao().insert(radioParameter)
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

fun scheduleRadioParametersBackgroundWork(sessionId: String, saveToDb: Boolean): OneTimeWorkRequest {
    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    val inputData = workDataOf(SESSION_ID to sessionId)

    val request: OneTimeWorkRequest = OneTimeWorkRequest.Builder(RadioParametersWorker::class.java)
        .setInputData(inputData)
        .addTag(WORKER_TAG)
        .setConstraints(constraints)
        .build()

    Log.v(TAG, request.id.toString())
    WorkManager.getInstance(QosApp.msWebApi.ctx).enqueueUniqueWork(WORKER_TAG, ExistingWorkPolicy.REPLACE, request)

    return request
}