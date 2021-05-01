package com.isel_5gqos.models

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.PROGRESS
import com.isel_5gqos.common.WORKER_TAG
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.Session
import com.isel_5gqos.dtos.RadioParametersDto
import com.isel_5gqos.dtos.SessionDto
import com.isel_5gqos.dtos.WrapperDto
import com.isel_5gqos.utils.DateUtils.Companion.formatDate
import com.isel_5gqos.workers.scheduleRadioParametersBackgroundWork
import java.lang.IllegalArgumentException
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

        //scheduleThroughPutBackgroundWork(sessionId = session.id)
    }

    fun endSession() {
        val endDate = Timestamp(System.currentTimeMillis())

        value.endDate = endDate

        val session = value.dtoToDaoMapper()

        asyncTask({ QosApp.db.sessionDao().updateSession(session)}) {}
        WorkManager.getInstance(QosApp.msWebApi.ctx).cancelAllWorkByTag(WORKER_TAG)
    }

    fun updateRadioParameters () {

        TODO("Ir buscar a todas as tabelas a infos e atualizar a activity")
    }
}