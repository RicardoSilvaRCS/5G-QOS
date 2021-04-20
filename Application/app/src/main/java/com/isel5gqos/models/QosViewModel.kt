package com.isel5gqos.models

import android.util.Log
import com.isel5gqos.Common.TAG
import com.isel5gqos.dtos.UserDto
import com.isel5gqos.Common.services.ManagementServiceWebApi
import com.isel5gqos.Common.repository.UserRepository
import com.isel5gqos.Common.services.api.Service

class QosViewModel(private val managementSystemApi: ManagementServiceWebApi,private val service: Service) : AbstractModel<UserDto>({ UserDto("", "") }) {
    companion object {
        val logins = mapOf("Afonso" to "Nobre", "Ricardo" to "Silva")
    }

    fun login(username: String, password: String):UserDto {

        Log.v(TAG, "*Logging In*")

        UserRepository(service).login("afonso.nobre@isel.pt", "i9bif4fGcmEn")//username, password)

        liveData.postValue(UserRepository(service).login("afonso.nobre@isel.pt", "i9bif4fGcmEn"))
        /*managementSystemApi.login("afonso.nobre@isel.pt", "i9bif4fGcmEn", { userDto ->
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
        })*/
    }
}