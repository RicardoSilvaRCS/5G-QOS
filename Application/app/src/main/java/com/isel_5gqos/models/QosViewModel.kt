package com.isel_5gqos.models

import android.widget.Toast
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.GENERIC_ERROR
import com.isel_5gqos.common.INVALID_CREDENTIALS
import com.isel_5gqos.common.NO_CONNECTION_ERROR
import com.isel_5gqos.common.TIMEOUT_ERROR
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.MobileUnit
import com.isel_5gqos.common.db.entities.User
import com.isel_5gqos.common.services.ManagementServiceWebApi
import com.isel_5gqos.dtos.UserDto
import com.isel_5gqos.utils.android_utils.AndroidUtils
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
                    AndroidUtils.makeBurnedToast(managementSystemApi.ctx, NO_CONNECTION_ERROR)
                }
                if(it is TimeoutError){
                    AndroidUtils.makeRawToast(managementSystemApi.ctx, TIMEOUT_ERROR)
                }
                else {
                    AndroidUtils.makeRawToast(managementSystemApi.ctx, INVALID_CREDENTIALS)
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

                }) {
                    liveData.postValue(user)
                }


            },
            onError = {
                if (it is NoConnectionError) {
                    AndroidUtils.makeBurnedToast(managementSystemApi.ctx, NO_CONNECTION_ERROR)
                }
                if(it is TimeoutError){
                    AndroidUtils.makeRawToast(managementSystemApi.ctx, TIMEOUT_ERROR)
                }
                else {
                    Toast.makeText(managementSystemApi.ctx, GENERIC_ERROR , Toast.LENGTH_LONG).show()
                }
            }
        )

    }

    fun refreshToken (username : String,token : String)  {
        managementSystemApi.refreshToken (
            authenticationToken = token,
            onSuccess = { refreshedToken ->

                asyncTask({

                    QosApp.db.userDao().updateToken(token,refreshedToken)

                }) {

                    liveData.postValue(
                        UserDto(username,refreshedToken)
                    )
                }
                
            },
            onError = {
                if (it is NoConnectionError) {
                    AndroidUtils.makeBurnedToast(managementSystemApi.ctx, NO_CONNECTION_ERROR)
                }
                if(it is TimeoutError){
                    AndroidUtils.makeBurnedToast(managementSystemApi.ctx, TIMEOUT_ERROR)
                }
                else {
                    AndroidUtils.makeBurnedToast(managementSystemApi.ctx, NO_CONNECTION_ERROR)
                }

                liveData.postValue(
                    UserDto("","")
                )
            }
        )
    }


    fun getLoggedUser () = QosApp.db.userDao().getLoggedUser()

}