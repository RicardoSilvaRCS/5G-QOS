package com.isel_5gqos.repositories

import android.util.Base64
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.INVALID_CREDENTIALS
import com.isel_5gqos.common.NO_CONNECTION_ERROR
import com.isel_5gqos.common.TIMEOUT_ERROR
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.Login
import com.isel_5gqos.common.db.entities.MobileUnit
import com.isel_5gqos.common.db.entities.TestPlanResult
import com.isel_5gqos.common.db.entities.User
import com.isel_5gqos.common.services.ManagementServiceWebApi
import com.isel_5gqos.dtos.MobileDeviceDto
import com.isel_5gqos.dtos.UserDto
import com.isel_5gqos.common.utils.android_utils.AndroidUtils
import com.isel_5gqos.common.utils.qos_utils.EventEnum
import com.isel_5gqos.common.utils.qos_utils.QoSUtils
import com.isel_5gqos.common.utils.qos_utils.SystemLogProperties

class QosRepository(private val managementSystemApi: ManagementServiceWebApi) {

    fun login(username: String, password: String, onSuccess: (UserDto) -> Unit, onError: (VolleyError) -> Unit) {
        managementSystemApi.login(
            username = username,
            password = password,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun logout(token: String, onSuccess: () -> Unit, onError: (VolleyError) -> Unit) = managementSystemApi.logout(
        token = token,
        onSuccess = onSuccess,
        onError = onError
    )

    fun postLoginResultToDb(user: User, onPostExecute: () -> Unit) {
        asyncTask({
            QosApp.db.userDao().insert(user)
        })
        {
            onPostExecute()
        }
    }

    fun insertUserLoginToDb(login: Login, onPostExecute: () -> Unit) {
        asyncTask({ QosApp.db.loginDao().insertUserLogin(login) }) {
            onPostExecute()
        }
    }

    fun insertMobileUnitSetting(mobileUnit: MobileUnit, onPostExecute: () -> Unit) {
        asyncTask(
            doInBackground = {

                QosApp.db.mobileUnit().insertMobileUnitSetting(mobileUnit)

            },
            onPostExecute = { onPostExecute() }
        )
    }

    fun refreshToken(token: String, onSuccess: (String) -> Unit, onError: (VolleyError) -> Unit) {
        managementSystemApi.refreshToken(token, onSuccess, onError)
    }

    fun updateTokenInDb(username: String, token: String, onPostExecute: () -> Unit) {
        asyncTask(
            doInBackground = {

                QosApp.db.loginDao().updateToken(token, username)

            },
            onPostExecute = onPostExecute
        )
    }

    fun loginDevice(serialNumber: String, user: UserDto, onSuccess: (MobileDeviceDto) -> Unit, onError: (VolleyError) -> Unit) {
        managementSystemApi.registerMobileDevice(
            mobileSerialNumber = serialNumber,
            authenticationToken = user.userToken,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun getLoggedUser() = QosApp.db.loginDao().getToken()

    fun deleteUserLogin(username: String) = QosApp.db.loginDao().logoutActiveUser(username = username)

    fun getDeviceId() = QosApp.db.mobileUnit().getMobileUnitSettings()

    fun getTestsByTestPlanId(testPlanId: String) = QosApp.db.testPlanResultDao().getTestPlanResults(testPlanId)

    fun getAllTestPlans() = QosApp.db.testPlanDao().getTestPlans()

    fun updateTestPlanResultState (testResult: TestPlanResult, isReported : Boolean = true) {
        asyncTask(
            doInBackground = {
                QosApp.db.testPlanResultDao().updateIsReported(testPlanId = testResult.testPlanId, testId = testResult.testId, isReported = isReported)
            }
        )
    }

    fun postTestPlanResult (testResult: TestPlanResult, token: String, deviceId : Int, onPostExecute: () -> Unit, onError: (VolleyError) -> Unit) {
        QosApp.msWebApi.postUnreportedResults(
            authenticationToken = token,
            deviceId = deviceId,
            testPlanResult = testResult.result,
            onSuccess = onPostExecute,
            onError = onError
        )
    }
}