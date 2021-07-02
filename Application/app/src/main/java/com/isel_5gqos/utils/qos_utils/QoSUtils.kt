package com.isel_5gqos.utils.qos_utils

import android.content.Context
import android.util.EventLog
import com.isel_5gqos.QosApp
import com.isel_5gqos.dtos.NavigationDto
import com.isel_5gqos.dtos.SystemLogDto
import com.isel_5gqos.utils.DateUtils
import com.isel_5gqos.utils.mobile_utils.LocationUtils
import java.util.*

class QoSUtils {

    companion object {

        //<editor-fold name="System Log Description Mapper">

        private val descriptionMap = mapOf(
            EventEnum.CONTROL_CONNECTION_OK to EmptyMapper(),
            EventEnum.CONTROL_CONNECTION_ERROR to MapperWithCause(),
            EventEnum.TESTPLAN_SCHEDULED to MapperWithTestPlanId(),
            EventEnum.TESTPLAN_STARTED to MapperWithTestPlanId(),
            EventEnum.TESTPLAN_FINISHED to MapperWithTestPlanId(),
            EventEnum.TESTPLAN_ERROR to MapperWithTestPlanIdAndCause(),
            EventEnum.TEST_START to MapperWithTestId(),
            EventEnum.TEST_END to MapperWithTestId(),
            EventEnum.TEST_ERROR to MapperWithTestIdAndCause(),
        )

        //</editor-fold>

        fun logToServer(
            token: String,
            deviceId: Int,
            event: EventEnum,
            context : Context,
            props : SystemLogProperties,
            onPostExec: () -> Unit = {},
        ) {

            val systemLog = SystemLogDto(
                date = DateUtils.getDateIso8601Format(),
                event = event.toString(),
                navigationDto = getProbeLocation(context),
                probeId = deviceId,
                properties = "{${descriptionMap[event]?.map(props)}}",
            )

            QosApp.msWebApi.systemLog(
                authenticationToken = token,
                deviceId = deviceId,
                systemLog = systemLog,
                onSuccess = {

                    onPostExec()

                },
                onError = {
                    onPostExec()
                }
            )

        }

        fun getProbeLocation (context: Context) : NavigationDto {

            val location = LocationUtils.getLocation(context)

            return NavigationDto(
                gpsFix = location?.provider ?: "",
                latitude = location?.latitude ?: 0.0,
                longitude = location?.longitude ?: 0.0,
                speed = location?.speed,
            )
        }
    }
}