package com.isel_5gqos.models

import android.util.Base64
import com.android.volley.AuthFailureError
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.MobileUnit
import com.isel_5gqos.common.db.entities.User
import com.isel_5gqos.common.db.entities.Login
import com.isel_5gqos.common.services.ManagementServiceWebApi
import com.isel_5gqos.dtos.UserDto
import com.isel_5gqos.utils.android_utils.AndroidUtils

class QosViewModel(private val managementSystemApi: ManagementServiceWebApi) : AbstractModel<UserDto>({ UserDto("", "",-1) }) {

    fun login(username: String, password: String,mobileId : String) {
        managementSystemApi.login(
            username = username,
            password = password,
            onSuccess = { userDto ->

                val user = User(
                    regId = QosApp.sessionId,
                    username = username,
                    timestamp = System.currentTimeMillis() + (60 * 1000).toLong(),
                    loggedOut = false,
                    credentials = Base64.encodeToString("${username}:${password}".toByteArray(charset("UTF-8")), Base64.DEFAULT).replace("\n", "")
                )

                val login = Login (
                    user = username,
                    token = userDto.userToken,
                    timestamp = System.currentTimeMillis() + (60 * 1000).toLong(),
                )

                asyncTask({

                    QosApp.db.userDao().insert(user)
                })
                {
                    asyncTask({ QosApp.db.loginDao().insertUserLogin(login) })
                    loginDevice(userDto,mobileId)
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

    private fun loginDevice(user: UserDto, mobileId : String) {
        managementSystemApi.registerMobileDevice(
            mobileSerialNumber = mobileId,
            authenticationToken = user.userToken,
            onSuccess = {

                val mobileUnit = MobileUnit(
                    mobileUnitId = it.mobileUnitId,
                    password = it.password,
                    controlConnectionHref = it.controlConnectionHref,
                    systemLogHref = it.systemLogHref
                )

                asyncTask(
                    doInBackground = {

                        QosApp.db.mobileUnit().insertMobileUnitSetting(mobileUnit)

                    },
                    onPostExecute = {

                        user.deviceId = it.mobileUnitId
                        liveData.postValue(user)

                    }
                )
            },
            onError = {

                when (it) {
                    is NoConnectionError -> AndroidUtils.makeRawToast(managementSystemApi.ctx, NO_CONNECTION_ERROR)
                    is TimeoutError -> AndroidUtils.makeRawToast(managementSystemApi.ctx, TIMEOUT_ERROR)
                    else -> AndroidUtils.makeRawToast(managementSystemApi.ctx, GENERIC_ERROR)
                }

                liveData.postValue(
                    UserDto("", "",-1)
                )

            }
        )

    }

    fun refreshToken(username: String, token: String, mobileId : String) {
        managementSystemApi.refreshToken(
            authenticationToken = token,
            onSuccess = { refreshedToken ->

                asyncTask(
                    doInBackground = {

                        QosApp.db.loginDao().updateToken(token, username)

                    },
                    onPostExecute = {

                        loginDevice(UserDto(username, refreshedToken,-1),mobileId)
                    }
                )

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
                    UserDto("", "",-1)
                )
            }
        )
    }

    fun getLoggedUser() = QosApp.db.userDao().getToken()

}