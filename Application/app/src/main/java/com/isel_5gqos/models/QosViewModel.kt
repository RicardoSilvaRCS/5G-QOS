package com.isel_5gqos.models

import android.util.Log
import android.widget.Toast
import com.android.volley.NoConnectionError
import com.isel_5gqos.Common.TAG
import com.isel_5gqos.Common.services.ManagementServiceWebApi
import com.isel_5gqos.dtos.UserDto

class QosViewModel(private val managementSystemApi: ManagementServiceWebApi) : AbstractModel<UserDto>({ UserDto("", "") }) {

    fun login(username: String, password: String) {
        managementSystemApi.login(
            username = username,
            password = password,
            onSuccess = { userDto ->
                //TODO: Store user info in db
                liveData.postValue(userDto)
            },
            onError = {
                if (it is NoConnectionError) {
                    Toast.makeText(managementSystemApi.ctx, "Please check your internet connection", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(managementSystemApi.ctx, "Invalid Credentials", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}