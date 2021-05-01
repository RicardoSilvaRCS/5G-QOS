package com.isel_5gqos.models

import android.widget.Toast
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.isel_5gqos.QosApp
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.User
import com.isel_5gqos.common.services.ManagementServiceWebApi
import com.isel_5gqos.dtos.UserDto

class QosViewModel(private val managementSystemApi: ManagementServiceWebApi) : AbstractModel<UserDto>({ UserDto("", "") }) {

    fun login(username: String, password: String) {
        managementSystemApi.login(
            username = "ricardo.silva@isel.pt",
            password = "maiZm3jiBPmW",
            onSuccess = { userDto ->

                val user = User(
                    regId = QosApp.sessionId,
                    username = "ricardo.silva@isel.pt",
                    token = userDto.userToken,
                    timestamp = System.currentTimeMillis() + (60 * 1000).toLong()
                )

                asyncTask({ QosApp.db.userDao().insert(user) }) {}

                liveData.postValue(userDto)
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
}