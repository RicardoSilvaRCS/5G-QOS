package com.isel_5gqos.models

import com.isel_5gqos.QosApp
import com.isel_5gqos.common.DEFAULT_SESSION_ID
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.Session
import com.isel_5gqos.dtos.SessionDto
import com.isel_5gqos.utils.DateUtils.Companion.formatDate
import java.sql.Timestamp
import java.util.*

class TestViewModel(val userName: String) : AbstractModel<SessionDto>({ SessionDto.makeDefault() }) {

    fun startSession(userName: String, onPostExecute: (sessionDto : SessionDto) -> Unit = {}) {
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

        asyncTask(
            doInBackground = { QosApp.db.sessionDao().insert(session) },
            onPostExecute = {
                onPostExecute(sessionDto)
                liveData.postValue(sessionDto)
            }
        )
    }

    fun endSessionByTag(workerTag: String) {
        val endDate = Timestamp(System.currentTimeMillis())

        value.endDate = endDate

        val session = value.dtoToDaoMapper()

        asyncTask({
            QosApp.db.sessionDao().updateSession(session)
        })
    }

    fun endSessionById(sessionId: String, endTime : Timestamp) {
        asyncTask(
            doInBackground = {
                QosApp.db.sessionDao().finishSessionById(sessionId,endTime.time)
            }
        )
    }

    fun updateSessionStartDate(sessionId: String) {
        asyncTask(doInBackground = {QosApp.db.sessionDao().updateStartSession(sessionId,System.currentTimeMillis())})
    }

    fun getLastSession() = QosApp.db.sessionDao().getLastSession()

    fun getCompletedSessions() = QosApp.db.sessionDao().getCompletedSessions()

    fun registerRadioParametersChanges(sessionId: String) = QosApp.db.radioParametersDao().getUpToDateRadioParameters(sessionId)

    fun getServingCells(sessionId: String) = QosApp.db.radioParametersDao().getServingCells(sessionId)

    fun getServingCell(sessionId: String) = QosApp.db.radioParametersDao().getServingCell(sessionId)

    fun registerThroughPutChanges(sessionId: String) = QosApp.db.throughPutDao().get(sessionId)

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

    fun deleteSessionInfo(sessionId: String,onPostExecute: () -> Unit) {
        asyncTask(
            doInBackground = {
                QosApp.db.sessionDao().deleteSession(sessionId)
            },
            onPostExecute = onPostExecute
        )
    }

    fun updateModel(session: Session){
        liveData.value =
            SessionDto(
                id = session.id,
                sessionName = session.sessionName,
                username = session.user,
                beginDate = Timestamp(session.beginDate),
                endDate = Timestamp(session.endDate)
           )
    }
}