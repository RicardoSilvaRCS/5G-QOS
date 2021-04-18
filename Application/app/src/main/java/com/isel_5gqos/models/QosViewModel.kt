package com.isel_5gqos.models

import android.util.Log
import android.widget.Toast
import com.android.volley.NoConnectionError
import com.isel_5gqos.Common.TAG
import com.isel_5gqos.dtos.UserDto
import com.isel_5gqos.repository.services.ManagementServiceWebApi

class QosViewModel(private val managementSystemApi: ManagementServiceWebApi) : AbstractModel<UserDto>({ UserDto("", "") }) {
    companion object {
        val logins = mapOf("Afonso" to "Nobre", "Ricardo" to "Silva")
    }

    fun login(username: String, password: String) {

        Log.v(TAG, "*Logging In*")

        managementSystemApi.login("afonso.nobre@isel.pt", "i9bif4fGcmEn", { userDto ->
            Log.v(TAG, "*Logging is valid!*")

            liveData.postValue(userDto)

        }, {
            Log.v(TAG, "*Invalid Login*")
            Log.v(TAG, it.message ?:"")
            if (it is NoConnectionError) {
                Toast.makeText(managementSystemApi.ctx, "Please check your internet connection", Toast.LENGTH_LONG).show()
            } else {
                //TODO : Ver Se há forma de identifcar quando é invalid credentials
                Toast.makeText(managementSystemApi.ctx, "Invalid Credentials", Toast.LENGTH_LONG).show()
            }
        })
    }
}