package com.isel_5gqos.workers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.*
import com.isel_5gqos.common.*
import com.isel_5gqos.dtos.RadioParametersDto


class RadioParametersWorker(private val context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {


    override fun doWork(): Result {

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
                var no = 0
                //allCellInfo[0] = serving cell, others = neighbor cells
                telephonyManager!!.allCellInfo.forEach {
                    if (it is CellInfoGsm) {
                        cellInfoList.add(
                            RadioParametersDto(
                                no = no++,
                                tech = "G${it.cellIdentity}",
                                arfcn = it.cellIdentity.arfcn,
                                rssi = if (it.cellConnectionStatus == CONNECTION_STATUS_UNKNOWN) MIN_RSSI else null,
                                cId = it.cellIdentity.cid,
                                netDataType = NetworkDataTypesEnum.GSM
                            )
                        )
                    } else if (it is CellInfoLte) {
                        cellInfoList.add(
                            RadioParametersDto(
                                no = no++,
                                tech = "L${it.cellIdentity.bandwidth}",
                                arfcn = it.cellIdentity.earfcn,
                                rssi = it.cellSignalStrength.rssi,
                                rsrp = it.cellSignalStrength.rsrp,
                                pci = it.cellIdentity.pci,
                                rssnr = it.cellSignalStrength.rssnr,
                                rsrq = it.cellSignalStrength.rsrq,
                                netDataType = NetworkDataTypesEnum.LTE
                            )
                        )
                    } else if (it is CellInfoWcdma) {
                        //Measure UMTS
                        cellInfoList.add(
                            RadioParametersDto(
                                no = no++,
                                tech = "U${it.cellIdentity}",
                                arfcn = it.cellIdentity.uarfcn,
                                rssi = if (it.cellConnectionStatus == CONNECTION_STATUS_UNKNOWN) MIN_RSSI else null,
                                psc = it.cellIdentity.psc,
                                netDataType = NetworkDataTypesEnum.UMTS
                            )
                        )
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && it is CellInfoNr) {
                        //Measure 5G
                        cellInfoList.add(
                            RadioParametersDto(
                                no = no++,
                                tech = "5G${it.cellIdentity}",
                                rssi = if (it.cellConnectionStatus == CONNECTION_STATUS_UNKNOWN) MIN_RSSI else null,
                                netDataType = NetworkDataTypesEnum.FiveG
                            )
                        )
                    }
                }

                cellInfoList.forEach {
                    it
                }

                //Network Operator Info
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                Log.v(TAG, "${telephonyManager.networkOperatorName} Network Operator name")
                Log.v(TAG, "${telephonyManager.networkOperator} MCC/MNC")
                Log.v(TAG, "${telephonyManager.imei} IMEI")
                Log.v(TAG, "${(telephonyManager.allCellInfo[0] as CellInfoLte).cellIdentity.tac} TAC")

                Thread.sleep(10000)

            } catch (ex: Exception) {

                //TODO: register new error to db

                return Result.failure()
            }


        } while (!workInfo.isCancelled)

        return Result.success()
    }
}

fun scheduleRadioParametersBackgroundWork(sessionId: String, isRecording: Boolean) {
    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    val inputData = workDataOf(SESSION_ID to sessionId)

    val request: OneTimeWorkRequest = OneTimeWorkRequest.Builder(RadioParametersWorker::class.java)
        .setInputData(inputData)
        .setConstraints(constraints)
        .build()

    Log.v(TAG, request.id.toString())
    WorkManager.getInstance(QoSApp.msWebApi.ctx).enqueueUniqueWork(WORKER_TAG, ExistingWorkPolicy.REPLACE, request)
}