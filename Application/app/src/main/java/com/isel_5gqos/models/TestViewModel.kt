package com.isel_5gqos.models

import android.util.Log
import androidx.work.WorkManager
import com.isel_5gqos.common.QoSApp
import com.isel_5gqos.common.TAG
import com.isel_5gqos.common.WORKER_TAG
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.Session
import com.isel_5gqos.dtos.SessionDto
import com.isel_5gqos.utils.DateUtils.Companion.formatDate
import com.isel_5gqos.workers.scheduleRadioParametersBackgroundWork
import com.isel_5gqos.workers.scheduleThroughPutBackgroundWork
import java.lang.IllegalArgumentException
import java.sql.Time
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
        liveData.postValue(sessionDto)

        val session = Session(
            id = sessionDto.id,
            sessionName = sessionDto.sessionName,
            user = sessionDto.username,
            beginDate = sessionDto.beginDate.time,
            endDate = sessionDto.endDate.time
        )
        asyncTask({ QoSApp.db.sessionDao().insert(session) }) {}

        //scheduleThroughPutBackgroundWork(sessionId = session.id)
        val requestId = scheduleRadioParametersBackgroundWork(sessionId = sessionDto.id, isRecording = true)

//        WorkManager.getInstance(context)
//            // requestId is the WorkRequest id
//            .getWorkInfoByIdLiveData(requestId.id)
//            .observe(observer, Observer { workInfo: WorkInfo? ->
//                if (workInfo != null) {
//                    val progress = workInfo.progress
//                    val value = progress.getInt(Progress, 0)
//                    // Do something with progress information
//                }
//            })

    }

    fun endSession() {
        val endDate = Timestamp(System.currentTimeMillis())

        value.endDate = endDate

        val session = value.dtoToDaoMapper()

        asyncTask({QoSApp.db.sessionDao().updateSession(session)}) {}
        WorkManager.getInstance(QoSApp.msWebApi.ctx).cancelAllWorkByTag(WORKER_TAG)
    }
}