package com.isel_5gqos.common.services.api

import com.isel_5gqos.dtos.UserDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface Service {
    @POST("user/login")
    fun login(@Header("Authorization") authorization:String): Call<UserDto>

}