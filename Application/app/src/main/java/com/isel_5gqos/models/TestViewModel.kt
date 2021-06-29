package com.isel_5gqos.models

import com.isel_5gqos.QosApp
import com.isel_5gqos.common.DEFAULT_SESSION_ID
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.Session
import com.isel_5gqos.dtos.SessionDto
import com.isel_5gqos.repositories.TestRepository
import com.isel_5gqos.utils.DateUtils
import com.isel_5gqos.utils.DateUtils.Companion.formatDate
import java.sql.Timestamp
import java.util.*

class TestViewModel(val userName: String,private val testRepository:TestRepository) : AbstractModel<SessionDto>({ SessionDto.makeDefault() }) {

    fun startSession(userName: String, onPostExecute: (sessionDto : SessionDto) -> Unit = {}) {
        if (userName.isBlank()) throw IllegalArgumentException("Username can't be empty")

        val currentDate = Date(System.currentTimeMillis())
        val dateFormatted: String = DateUtils.formatDate(currentDate)

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

        testRepository.startSession(session) {
            liveData.postValue(sessionDto)
            onPostExecute(sessionDto)
        }
    }

    fun endSessionByTag(workerTag: String) {
        val endDate = Timestamp(System.currentTimeMillis())

        value.endDate = endDate

        val session = value.dtoToDaoMapper()

        testRepository.endSessionByTag(workerTag,session)
    }

    fun endSessionById(sessionId: String, endTime : Timestamp) {
        testRepository.endSessionById(sessionId, endTime)
    }

    fun updateSessionStartDate(sessionId: String) {
        testRepository.updateSessionStartDate(sessionId)
    }

    fun getSessionInfo(sessionId: String) = testRepository.getSessionInfo(sessionId)

    fun getLastSession() = testRepository.getLastSession()

    fun getCompletedSessions() = testRepository.getCompletedSessions()

    fun registerRadioParametersChanges(sessionId: String) = testRepository.registerRadioParametersChanges(sessionId)

    fun getServingCells(sessionId: String) = testRepository.getServingCells(sessionId)

    fun getServingCell(sessionId: String) = testRepository.getServingCell(sessionId)

    fun getAllRadioParameters() = testRepository.getAllRadioParameters()

    fun getAllThroughputs() = testRepository.getAllThroughputs()

    fun getRadioParametersBySessionId(sessionId: String) = testRepository.getRadioParametersBySessionId(sessionId)

    fun getThroughputBySessionId(sessionId: String) = testRepository.getThroughputBySessionId(sessionId)

    fun registerThroughPutChanges(sessionId: String) = testRepository.registerThroughPutChanges(sessionId)

    fun startDefaultSession(userName: String) {
        val currentDate = Date(System.currentTimeMillis())
        val dateFormatted: String = DateUtils.formatDate(currentDate)

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
        testRepository.startDefaultSession(session){
            liveData.postValue(sessionDto)
        }

    }

    fun deleteSessionInfo(sessionId: String,onPostExecute: () -> Unit) {
        testRepository.deleteSessionInfo(sessionId, onPostExecute)
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