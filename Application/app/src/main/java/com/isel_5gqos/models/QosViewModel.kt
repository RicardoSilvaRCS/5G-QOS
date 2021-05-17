package com.isel_5gqos.models

import android.widget.Toast
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.MobileUnit
import com.isel_5gqos.common.db.entities.User
import com.isel_5gqos.common.services.ManagementServiceWebApi
import com.isel_5gqos.dtos.UserDto
import com.isel_5gqos.utils.mobile_utils.MobileInfoUtils

class QosViewModel(private val managementSystemApi: ManagementServiceWebApi) : AbstractModel<UserDto>({ UserDto("", "") }) {

    fun login(username: String, password: String) {
        managementSystemApi.login(
            username = username,
            password = password,
            onSuccess = { userDto ->

                val user = User(
                    regId = QosApp.sessionId,
                    username = username,
                    token = userDto.userToken,
                    timestamp = System.currentTimeMillis() + (60 * 1000).toLong(),
                    loggedOut = false
                )


                asyncTask({

                    QosApp.db.userDao().insert(user)

                })
                {

                    loginDevice(userDto)

                }

            },
            onError = {
                if (it is NoConnectionError) {
                    Toast.makeText(managementSystemApi.ctx, "Please check your internet connection", Toast.LENGTH_LONG).show()
                }
                if(it is TimeoutError){
                    Toast.makeText(managementSystemApi.ctx, "There is a problem with the server", Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(managementSystemApi.ctx, "Invalid Credentials", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun loginDevice(user: UserDto) {

        managementSystemApi.registerMobileDevice(
            mobileSerialNumber = MobileInfoUtils.getDeviceSerialNumber(),
            authenticationToken = user.userToken,
            onSuccess = {

                val mobileUnit = MobileUnit(
                     mobileUnitId = it.mobileUnitId,
                     password =  it.password,
                     controlConnectionHref = it.controlConnectionHref,
                     systemLogHref = it.systemLogHref
                )

                asyncTask({

                    QosApp.db.mobileUnit().insertMobileUnitSetting(mobileUnit)

                }) {}

                liveData.postValue(user)

            },
            onError = {
                if (it is NoConnectionError) {
                    Toast.makeText(managementSystemApi.ctx, "Please check your internet connection", Toast.LENGTH_LONG).show()
                }
                if(it is TimeoutError){
                    Toast.makeText(managementSystemApi.ctx, "There is a problem with the server", Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(managementSystemApi.ctx, it.message, Toast.LENGTH_LONG).show()
                }
            }
        )

    }

    fun refreshToken () {
        //TODO IR á bd buscar um user ativo e dar refresh na token
    }
}