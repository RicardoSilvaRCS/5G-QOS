package com.isel_5gqos.Common

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import com.isel_5gqos.Common.db.QosDb
import com.isel_5gqos.Common.services.ManagementServiceWebApi
import java.util.*

class QoSApp : Application() {
    companion object {
        lateinit var msWebApi: ManagementServiceWebApi
        lateinit var db:QosDb
        val sessionId = UUID.randomUUID().toString()
    }

    override fun onCreate() {
        super.onCreate()
        msWebApi = ManagementServiceWebApi(applicationContext)
        db = Room.databaseBuilder(applicationContext,QosDb::class.java,"Qos-Db").build()
    }
}