package com.isel_5gqos.repositories

import com.android.volley.VolleyError
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.Login
import com.isel_5gqos.common.db.entities.MobileUnit
import com.isel_5gqos.common.db.entities.TestPlanResult
import com.isel_5gqos.common.db.entities.User
import com.isel_5gqos.common.services.ManagementServiceWebApi
import com.isel_5gqos.dtos.MobileDeviceDto
import com.isel_5gqos.dtos.UserDto

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

    fun getFinishedTestPlans() = QosApp.db.testPlanDao().getFinishedTestPlans()

    fun updateTestPlanResultState(testResult: TestPlanResult, isReported: Boolean = true) {
        asyncTask(
            doInBackground = {
                QosApp.db.testPlanResultDao()
                    .updateIsReported(testPlanId = testResult.testPlanId, testId = testResult.testId, isReported = isReported)
            }
        )
    }

    fun postTestPlanResult(testResult: TestPlanResult, token: String, deviceId: Int, onPostExecute: () -> Unit, onError: (VolleyError) -> Unit) {
        QosApp.msWebApi.postUnreportedResults(
            authenticationToken = token,
            deviceId = deviceId,
            testPlanResult = testResult.result,
            onSuccess = onPostExecute,
            onError = onError
        )
    }

    fun deleteTestPlanById(testPlanId: String, onPostExecute: () -> Unit = {}) {
        asyncTask(
            doInBackground = {
                QosApp.db.testPlanDao().deleteTestPlanById(testPlanId)
            },
            onPostExecute = onPostExecute
        )
    }
}