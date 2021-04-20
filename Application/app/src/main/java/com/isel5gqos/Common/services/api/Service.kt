package com.isel5gqos.Common.services.api

import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.POST

interface Service {
    @POST("user/login")
    fun login(@Header("Authorization") authorization:String): Call<Unit>

}