package com.isel_5gqos.utils.qos_utils

import android.content.Context
import com.isel_5gqos.QosApp
import com.isel_5gqos.dtos.NavigationDto
import com.isel_5gqos.dtos.RadioParametersDto
import com.isel_5gqos.dtos.SystemLogDto
import com.isel_5gqos.utils.DateUtils
import com.isel_5gqos.utils.mobile_utils.LocationUtils
import java.util.*

class QoSUtils {

    companion object {

        fun logToServer(
            token: String,
            deviceId: Int,
            event: EventEnum,
            level: LevelEnum,
            description: String, //description not implemented by server
            context : Context,
            onPostExec: () -> Unit
        ) {

            val systemLog = SystemLogDto(
                date = DateUtils.getDateIso8601Format(),
                event = event.eventType,
                id = Random().nextInt(),
                level = level.levelType,
                navigationDto = getProbeLocation(context),
                probeId = deviceId,
                properties = "{}",
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