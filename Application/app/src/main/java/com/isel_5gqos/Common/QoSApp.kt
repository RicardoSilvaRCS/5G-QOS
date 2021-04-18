package com.isel_5gqos.common

import android.app.Application
import com.isel_5gqos.repository.services.ManagementServiceWebApi

class QoSApp : Application() {
    companion object {
        lateinit var msWebApi: ManagementServiceWebApi
    }

    override fun onCreate() {
        super.onCreate()
        msWebApi = ManagementServiceWebApi(applicationContext)
    }
}