package com.isel_5gqos

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import com.isel_5gqos.common.CHANNEL_ID
import com.isel_5gqos.common.DATABASE_NAME
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
        db = Room.databaseBuilder(applicationContext, QosDb::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

        createNotificationChannel()
    }


    fun createNotificationChannel() {

        // 1. Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = "5GQos"
        val descriptionText = "5GQoS Notification"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        // 2. Register the channel with the system
        val notificationManager: NotificationManager =
            msWebApi.ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

}


