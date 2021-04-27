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
import com.isel_5gqos.common.QoSApp
import com.isel_5gqos.common.SESSION_ID
import com.isel_5gqos.common.TAG
import com.isel_5gqos.common.WORKER_TAG
import com.isel_5gqos.dtos.RadioParameters


class RadioParametersWorker (private val context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {


    override fun doWork(): Result {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Result.failure()
        }

        val workInfo = WorkManager.getInstance(context).getWorkInfoById(this.id)
        val sessionId = inputData.getString(SESSION_ID).toString()

        val telephonyManager = getSystemService(context, TelephonyManager::class.java)

        do{

            try{

                //Needed to make this way because of the compatibility of the Api.
                //The found correct form was only available at Api 29 reducing to much the compatibility of this app
                //The most correct way was to call telephonyManager!!.signalStrength!!.cellSignalStrengths and catch the cellSignal that is instance of CellSignalStrengthLte
                val info = telephonyManager!!.signalStrength!!.toString().split(" ")

                val rsrp = info[9]
                val rsrq = info[10]
                val rssnr = (info[11].toFloat()/10)
                val networkDataType = info[13]

                Log.v(TAG,"$rsrp RSRQ DBm")
                Log.v(TAG,"$rsrq RSRQ DB")
                Log.v(TAG,"$rssnr RSSNR DB")
                Log.v(TAG,"$info INFO") //Network Data Type 0= LTE

                val cellInfoList : MutableList<RadioParameters> = mutableListOf()
                var no = 0
                telephonyManager.allCellInfo.forEach{

                    val cellInfo : RadioParameters

                    if (it is CellInfoGsm) {
                        cellInfo = RadioParameters(
                            no = no++,
                            tech = "G${it.cellSignalStrength}",
                            arfcn = it.cellIdentity.arfcn,
                            rssi = 5F,
                            rsrp = 5F,
                            cId = it.cellIdentity.cid,s
                            psc = it.cellIdentity.psc,
                            netDataType = "GSM"
                        )
                        val mobileOperator = it.cellIdentity.mobileNetworkOperator
                        val cid = it.cellIdentity.lac
                    }

                    else if (it is CellInfoLte){

                    }
                    else if (it is CellInfoWcdma){
                        //Measure UMTS

                    }
                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && it is CellInfoNr) {
                        //Measure 5G
                    }

                }


                //Network Operator Info
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                Log.v(TAG,"${telephonyManager.networkOperatorName} Network Operator name")
                Log.v(TAG,"${telephonyManager.networkOperator} MCC/MNC")
                Log.v(TAG,"${telephonyManager.imei} IMEI")
                Log.v(TAG,"${(telephonyManager.allCellInfo[0] as CellInfoLte).cellIdentity.tac} TAC")

                Thread.sleep(10000)

            }catch (ex : Exception){

                //TODO: register new error to db

                return Result.failure()
            }


        }while(!workInfo.isCancelled)

        return Result.success()
    }
}

fun scheduleRadioParametersBackgroundWork(sessionId: String, isRecording : Boolean) {
    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    val inputData = workDataOf(SESSION_ID to sessionId)

    val request: OneTimeWorkRequest = OneTimeWorkRequest.Builder(RadioParametersWorker::class.java)
        .setInputData(inputData)
        .setConstraints(constraints)
        .build()

    Log.v(TAG,request.id.toString())
    WorkManager.getInstance(QoSApp.msWebApi.ctx).enqueueUniqueWork(WORKER_TAG, ExistingWorkPolicy.REPLACE,request)
}