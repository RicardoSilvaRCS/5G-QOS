package com.isel5gqos.Common

import android.app.Application
import com.isel5gqos.Common.services.MANAGEMENT_SYSTEM_URL
import com.isel5gqos.Common.services.ManagementServiceWebApi
import com.isel5gqos.Common.services.api.Service
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QoSApp : Application() {
    companion object {
        lateinit var msWebApi: ManagementServiceWebApi
        lateinit var api:Service
    }

    override fun onCreate() {
        super.onCreate()
        msWebApi = ManagementServiceWebApi(applicationContext)

        val retrofit = Retrofit.Builder().baseUrl(MANAGEMENT_SYSTEM_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(Service::class.java)
    }
}