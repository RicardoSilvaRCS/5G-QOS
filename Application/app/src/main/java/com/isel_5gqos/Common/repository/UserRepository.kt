package com.isel_5gqos.common.repository

import android.content.ContentValues.TAG
import android.util.Base64
import android.util.Log
import com.isel_5gqos.common.QoSApp.Companion.api
import com.isel_5gqos.dtos.UserDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRepository {

    fun login(username:String,password:String) {

        val authHeader = "Basic ${Base64.encodeToString("${username}:${password}".toByteArray(charset("UTF-8")), Base64.DEFAULT).replace("\n","")}"
        val call = api.login(authHeader)

        call.enqueue(UserCallback())

    }
}

class UserCallback : Callback<UserDto> {
    override fun onResponse(call: Call<UserDto>, response: Response<UserDto>) {
        Log.v(TAG,call.isExecuted.toString())
        val headers = response.headers()
        Log.v(TAG,headers.toString())
    }

    override fun onFailure(call: Call<UserDto>, t: Throwable) {
        Log.v(TAG,call.isCanceled.toString())
    }

}