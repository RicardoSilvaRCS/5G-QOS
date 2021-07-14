package com.isel_5gqos.jobs.jobs

import android.content.Context
import android.telephony.TelephonyManager
import com.isel_5gqos.QosApp.Companion.db
import com.isel_5gqos.common.db.entities.RadioParameters
import com.isel_5gqos.jobs.JobParametersEnum
import com.isel_5gqos.common.utils.errors.Exceptions
import com.isel_5gqos.common.utils.mobile_utils.RadioParametersUtils
import java.util.*

class RadioParametersJob : IJob {

    override fun job(params: Map<JobParametersEnum, Any?>) {

        val telephonyManager: TelephonyManager = params[JobParametersEnum.TelephonyManager] as TelephonyManager
        val sessionId: String = params[JobParametersEnum.SessionId] as String
        val context: Context = params[JobParametersEnum.Context] as Context

        try {

            val cellInfoList = RadioParametersUtils.getRadioParameters(telephonyManager,context)

            insertRadioParametersInfoInDb(
                sessionId,
                cellInfoList
            )

        } catch (ex: Exception) {
            Exceptions(ex)
        }

    }

    private fun insertRadioParametersInfoInDb(sessionId: String, radioParameters: List<RadioParameters>) {

        db.radioParametersDao().invalidateRadioParameters(sessionId)

        val servingCell = RadioParametersUtils.getServingCell(radioParameters)
        val numbOfCellsWithSameTechAsServing = radioParameters.filter { it.tech == servingCell.tech }

        val radioParams = radioParameters.map { radioParametersDto ->

            RadioParameters(
                regId = UUID.randomUUID().toString(),
                no = radioParametersDto.no,
                tech = radioParametersDto.tech?: "",
                arfcn = radioParametersDto.arfcn ?: -1,
                rssi = radioParametersDto.rssi ?: -1,
                rsrp = radioParametersDto.rsrp ?: -1,
                cId = radioParametersDto.cId ?: -1,
                psc = radioParametersDto.psc ?: -1,
                pci = radioParametersDto.pci ?: -1,
                rssnr = radioParametersDto.rssnr ?: -1,
                rsrq = radioParametersDto.rsrq ?: -1,
                netDataType = radioParametersDto.netDataType.toString(),
                isServingCell = radioParametersDto.isServingCell,
                numbOfCellsWithSameTechAsServing = numbOfCellsWithSameTechAsServing.size - 1, //Remove Serving Cell
                latitude =  radioParametersDto.latitude,
                longitude = radioParametersDto.longitude,
                sessionId = sessionId,
                timestamp = System.currentTimeMillis(),
                isUpToDate = true
            )

        }.toTypedArray()

        db.radioParametersDao().insert(*radioParams)

    }

    override fun getJobTimeout(): Long = 1000L

    override fun getJobParameters(): Array<JobParametersEnum> = arrayOf(JobParametersEnum.TelephonyManager, JobParametersEnum.SessionId, JobParametersEnum.Context)


}