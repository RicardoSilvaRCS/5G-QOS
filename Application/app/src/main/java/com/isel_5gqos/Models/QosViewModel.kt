package com.isel_5gqos.Models

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.android.volley.NoConnectionError
import com.isel_5gqos.Common.TAG
import com.isel_5gqos.Dtos.UserDto
import com.isel_5gqos.Repository.Services.ManagementServiceWebApi

class QosViewModel (private val managementSystemApi : ManagementServiceWebApi) : ViewModel() {
    companion object {
        val logins = mapOf("Afonso" to "Nobre", "Ricardo" to "Silva")
    }

    fun login(username: String, password: String): UserDto? {

        Log.v(TAG,"*Logging In*")
        var user : UserDto? = null
        managementSystemApi.login(username,password, { userDto ->
            Log.v(TAG,"*Logging is valid!*")

            user = userDto

        }, {
                Log.v(TAG,"*Invalid Login*")
                if(it is NoConnectionError){
                    Toast.makeText(managementSystemApi.ctx, "Please check your internet connection", Toast.LENGTH_LONG).show()
                } else{
                    //TODO : Ver Se há forma de identifcar quando é invalid credentials
                    Toast.makeText(managementSystemApi.ctx, "Invalid Credentials", Toast.LENGTH_LONG).show()
                }
            }
        )

        return user
    }
}