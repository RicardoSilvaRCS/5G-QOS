package com.isel_5gqos

import android.app.Application
import androidx.room.Room
import com.isel_5gqos.common.db.QosDb
import com.isel_5gqos.common.services.ManagementServiceWebApi
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class QosApp : Application() {
    companion object {
        lateinit var msWebApi: ManagementServiceWebApi
        lateinit var db: QosDb
        val sessionId = UUID.randomUUID().toString()
        val workersAvailable = ConcurrentLinkedQueue<String>()
    }

    override fun onCreate() {
        super.onCreate()
        msWebApi = ManagementServiceWebApi(applicationContext)
        db = Room.databaseBuilder(applicationContext, QosDb::class.java, "Qos-Db").build()
    }


}


