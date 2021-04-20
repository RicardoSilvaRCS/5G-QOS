package com.isel5gqos.Common.repository

import android.util.Base64
import android.util.Log
import com.isel5gqos.Common.TAG
import com.isel5gqos.Common.services.api.Service
import com.isel5gqos.dtos.UserDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await

class UserRepository(private val serviceApi:Service) {
    lateinit var userDto: UserDto
    lateinit var userName: String
    inner class UserCallback : Callback<Unit> {
        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
            Log.v(TAG,call.isExecuted.toString())
            val headers = response.headers()
            val token = headers.value(0).split(" ")[1]
            userDto = UserDto(userName,token)
            Log.v(TAG,headers.toString())
        }

        override fun onFailure(call: Call<Unit>, t: Throwable) {
            Log.v(TAG,call.isCanceled.toString())
        }
    }

    fun login(username:String, password:String):UserDto {
        userName = username
        val authHeader = "Basic ${Base64.encodeToString("${username}:${password}".toByteArray(charset("UTF-8")), Base64.DEFAULT)}".replace("\n","")
        val call = serviceApi.login(authHeader)

        call.enqueue(UserCallback())
        return userDto
    }
}

