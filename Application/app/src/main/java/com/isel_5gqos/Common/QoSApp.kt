package com.isel_5gqos.Common

import android.app.Application
import com.isel_5gqos.Common.services.ManagementServiceWebApi

class QoSApp : Application() {
    companion object {
        lateinit var msWebApi: ManagementServiceWebApi
    }

    override fun onCreate() {
        super.onCreate()
        msWebApi = ManagementServiceWebApi(applicationContext)
    }
}