package com.isel_5gqos.models

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.DEFAULT_SESSION_ID
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.RadioParameters
import com.isel_5gqos.common.db.entities.Session
import com.isel_5gqos.common.db.entities.ThroughPut
import com.isel_5gqos.dtos.RadioParametersDto
import com.isel_5gqos.dtos.SessionDto
import com.isel_5gqos.utils.DateUtils.Companion.formatDate
import java.sql.Timestamp
import java.util.*

class TestViewModel : AbstractModel<SessionDto>({ SessionDto.makeDefault() }) {

    fun startSession(userName: String) {
        if (userName.isBlank()) throw IllegalArgumentException("Username can't be empty")

        val currentDate = Date(System.currentTimeMillis())
        val dateFormatted: String = formatDate(currentDate)

        val sessionDto = SessionDto(
            id = UUID.randomUUID().toString(),
            sessionName = "Session $dateFormatted",
            username = userName,
            beginDate = Timestamp(System.currentTimeMillis())
        )

        val session = Session(
            id = sessionDto.id,
            sessionName = sessionDto.sessionName,
            user = sessionDto.username,
            beginDate = sessionDto.beginDate.time,
            endDate = sessionDto.endDate.time
        )

        asyncTask({ QosApp.db.sessionDao().insert(session) }) {}

        liveData.postValue(sessionDto)
    }

    fun registerRadioParametersChanges (sessionId : String) = QosApp.db.radioParametersDao().getUpToDateRadioParameters(sessionId)

    fun getServingCell (sessionId : String) = QosApp.db.radioParametersDao().getServingCell(sessionId)

    fun registerThroughPutChanges (sessionId : String) = QosApp.db.throughPutDao().get(sessionId)

    fun startDefaultSession(userName: String) {
        val currentDate = Date(System.currentTimeMillis())
        val dateFormatted: String = formatDate(currentDate)

        val sessionDto = SessionDto(
            id = DEFAULT_SESSION_ID,
            sessionName = "Session $dateFormatted",
            username = userName,
            beginDate = Timestamp(System.currentTimeMillis())
        )

        val session = Session(
            id = sessionDto.id,
            sessionName = sessionDto.sessionName,
            user = sessionDto.username,
            beginDate = sessionDto.beginDate.time,
            endDate = sessionDto.endDate.time
        )

        asyncTask({ QosApp.db.sessionDao().insert(session) }) { liveData.postValue(sessionDto) }
    }

    fun endSession(workerTag: String) {
        val endDate = Timestamp(System.currentTimeMillis())

        value.endDate = endDate

        val session = value.dtoToDaoMapper()

        asyncTask({
            QosApp.db.sessionDao().updateSession(session)
        }) {}
    }

    fun updateRadioParameters(id: String = "", lifecycleOwner: LifecycleOwner) {
        QosApp.db.radioParametersDao().getUpToDateRadioParameters(if (id == "") value.id else id).observe(lifecycleOwner) {
            val radioParametersDto = mutableListOf<RadioParametersDto>()
            it.forEach { radioParameters ->

                radioParametersDto.add(
                    RadioParametersDto(
                        no = radioParameters.no,
                        tech = radioParameters.tech,
                        arfcn = radioParameters.arfcn,
                        rssi = radioParameters.rssi,
                        rsrp = radioParameters.rsrp,
                        cId = radioParameters.cId,
                        psc = radioParameters.psc,
                        pci = radioParameters.pci,
                        rssnr = radioParameters.rssnr,
                        rsrq = radioParameters.rsrq,
                        netDataType = enumValueOf(radioParameters.netDataType),
                        isServingCell = radioParameters.isServingCell
                    )
                )
            }

            //TODO Possible pass this to a mapper
            val session = value
            session.radioParameters.radioParametersDtos = radioParametersDto
            liveData.postValue(session)
        }
    }

}