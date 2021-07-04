package com.isel_5gqos.repositories

import com.isel_5gqos.QosApp
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.Session
import java.sql.Timestamp

class TestRepository {
    fun startSession(session: Session, onPostExecute: () -> Unit = {}) {
        asyncTask(
            doInBackground = { QosApp.db.sessionDao().insert(session) },
            onPostExecute = {
                onPostExecute()
            }
        )
    }
    fun endSessionByTag(workerTag: String,session: Session) {


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

    fun getSessionInfo(sessionId: String) = QosApp.db.sessionDao().get(sessionId)

    fun getLastSession() = QosApp.db.sessionDao().getLastSession()

    fun getCompletedSessions(userName: String) = QosApp.db.sessionDao().getCompletedSessions(userName)

    fun registerRadioParametersChanges(sessionId: String) = QosApp.db.radioParametersDao().getUpToDateRadioParameters(sessionId)

    fun getServingCells(sessionId: String) = QosApp.db.radioParametersDao().getServingCells(sessionId)

    fun getServingCell(sessionId: String) = QosApp.db.radioParametersDao().getServingCell(sessionId)

    fun getAllRadioParameters() = QosApp.db.radioParametersDao().getAllRadioParameters()

    fun getAllThroughputs() = QosApp.db.throughPutDao().getAllThroughputs()

    fun getRadioParametersBySessionId(sessionId: String) = QosApp.db.radioParametersDao().get(sessionId)

    fun getThroughputBySessionId(sessionId: String) = QosApp.db.throughPutDao().get(sessionId)

    fun registerThroughPutChanges(sessionId: String) = QosApp.db.throughPutDao().get(sessionId)

    fun startDefaultSession(session: Session,onPostExecute: () -> Unit) {
        asyncTask({ QosApp.db.sessionDao().insert(session) }) { onPostExecute(); }
    }

    fun deleteSessionInfo(sessionId: String,onPostExecute: () -> Unit) {
        asyncTask(
            doInBackground = {
                QosApp.db.sessionDao().deleteSession(sessionId)
            },
            onPostExecute = onPostExecute
        )
    }

}