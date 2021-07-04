package com.isel_5gqos.models

import com.android.volley.AuthFailureError
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.Login
import com.isel_5gqos.common.db.entities.MobileUnit
import com.isel_5gqos.common.db.entities.User
import com.isel_5gqos.common.services.ManagementServiceWebApi
import com.isel_5gqos.dtos.UserDto
import com.isel_5gqos.repositories.QosRepository
import com.isel_5gqos.common.utils.android_utils.AndroidUtils

class QosViewModel(private val managementSystemApi: ManagementServiceWebApi, private val qosRepository: QosRepository) :
    AbstractModel<UserDto>({ UserDto("", "", -1) }) {

    fun login(username: String, password: String, serialNumber: String) {
        qosRepository.login(
            username,
            password,
            onSuccess = { userDto ->

                val user = User(
                    regId = QosApp.sessionId,
                    username = username,
                    timestamp = System.currentTimeMillis() + (60 * 1000).toLong(),
                )

                val login = Login(
                    user = username,
                    token = userDto.userToken,
                    timestamp = System.currentTimeMillis() + (60 * 1000).toLong(),
                )

                qosRepository.postLoginResultToDb(user) {
                    qosRepository.insertUserLoginToDb(login) {}
                    loginDevice(userDto, serialNumber)
                }
            },
            onError = {
                if (it is NoConnectionError) {
                    AndroidUtils.makeBurnedToast(managementSystemApi.ctx, NO_CONNECTION_ERROR)
                }
                if (it is TimeoutError) {
                    AndroidUtils.makeRawToast(managementSystemApi.ctx, TIMEOUT_ERROR)
                } else {
                    AndroidUtils.makeRawToast(managementSystemApi.ctx, INVALID_CREDENTIALS)
                }
            }
        )
    }


    fun refreshToken(username: String, token: String, mobileId: String) {
        qosRepository.refreshToken(
            token,
            onSuccess = {
                qosRepository.updateTokenInDb(username, token) {
                    loginDevice(UserDto(username, it, -1), mobileId)
                }
            },
            onError = {
                when (it) {
                    is NoConnectionError -> {
                        AndroidUtils.makeRawToast(managementSystemApi.ctx, NO_CONNECTION_ERROR)
                    }
                    is TimeoutError -> {
                        AndroidUtils.makeRawToast(managementSystemApi.ctx, TIMEOUT_ERROR)
                    }
                    is AuthFailureError -> {
                        AndroidUtils.makeRawToast(managementSystemApi.ctx, AUTH_FAILED_ERROR)
                    }
                    else -> {
                        AndroidUtils.makeRawToast(managementSystemApi.ctx, GENERIC_ERROR)
                    }
                }

                liveData.postValue(
                    UserDto("", "", -1)
                )
            }
        )
    }

    private fun loginDevice(user: UserDto, serialNumber: String) {

        qosRepository.loginDevice(
            serialNumber,
            user,
            onSuccess = {
                val mobileUnit = MobileUnit(
                    mobileUnitId = it.mobileUnitId,
                    password = it.password,
                    controlConnectionHref = it.controlConnectionHref,
                    systemLogHref = it.systemLogHref
                )
                qosRepository.insertMobileUnitSetting(mobileUnit) {
                    user.deviceId = it.mobileUnitId
                    liveData.postValue(user)
                }
            },
            onError = {
                when (it) {
                    is NoConnectionError -> AndroidUtils.makeRawToast(managementSystemApi.ctx, NO_CONNECTION_ERROR)
                    is TimeoutError -> AndroidUtils.makeRawToast(managementSystemApi.ctx, TIMEOUT_ERROR)
                    else -> AndroidUtils.makeRawToast(managementSystemApi.ctx, GENERIC_ERROR)
                }

                liveData.postValue(
                    UserDto("", "", -1)
                )
            }
        )

    }

    fun getLoggedUser() = qosRepository.getLoggedUser()

    fun getDeviceId () = qosRepository.getDeviceId()

    fun logoutActiveUser(username: String, token: String, onPostExec : () -> Unit) {
        qosRepository.logout(
            token,
            onSuccess = {
                asyncTask({qosRepository.deleteUserLogin(username = username)})
                onPostExec()
            },
            onError = {
                if (it is NoConnectionError) {
                    AndroidUtils.makeBurnedToast(managementSystemApi.ctx, NO_CONNECTION_ERROR)
                }
                if (it is TimeoutError) {
                    AndroidUtils.makeRawToast(managementSystemApi.ctx, TIMEOUT_ERROR)
                } else {
                    AndroidUtils.makeRawToast(managementSystemApi.ctx, INVALID_CREDENTIALS)
                }
            }
        )
    }

}