package com.isel_5gqos.models

import com.isel_5gqos.common.DEFAULT_SESSION_ID
import com.isel_5gqos.common.db.entities.Session
import com.isel_5gqos.repositories.TestRepository
import com.isel_5gqos.common.utils.DateUtils
import java.sql.Timestamp
import java.util.*

class TestViewModel(val userName: String,private val testRepository:TestRepository) : AbstractModel<Session>({ Session() }) {

    fun startSession(userName: String, onPostExecute: (session: Session) -> Unit) {
        if (userName.isBlank()) throw IllegalArgumentException("Username can't be empty")

        val currentDate = Date(System.currentTimeMillis())
        val dateFormatted: String = DateUtils.formatDate(currentDate)

        val session = Session(
            id = UUID.randomUUID().toString(),
            sessionName = "Session $dateFormatted",
            user = userName,
            beginDate = System.currentTimeMillis()
        )

        testRepository.startSession(session) {
            liveData.postValue(session)
            onPostExecute(session)
        }
    }

    fun endSessionByTag(workerTag: String) {
        val endDate = System.currentTimeMillis()

        value.endDate = endDate

        val session = value

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

    fun getCompletedSessions(userName: String) = testRepository.getCompletedSessions(userName)

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

        val session = Session(
            id = DEFAULT_SESSION_ID,
            sessionName = "Session $dateFormatted",
            user = userName,
            beginDate = System.currentTimeMillis()
        )

        testRepository.startDefaultSession(session){
            liveData.postValue(session)
        }

    }

    fun deleteSessionInfo(sessionId: String,onPostExecute: () -> Unit) {
        testRepository.deleteSessionInfo(sessionId, onPostExecute)
    }

    fun updateModel(session: Session){
        liveData.value =
            Session(
                id = session.id,
                sessionName = session.sessionName,
                user = session.user,
                beginDate = session.beginDate,
                endDate = session.endDate
           )
    }
}