package com.isel_5gqos.utils.android_utils

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.isel_5gqos.common.CHANNEL_ID

class AndroidUtils {

    companion object {

        fun makeBurnedToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        fun makeRawToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun notifyOnChannel(title: String, content: String, activity:  Class<*>?, context: Context) {

            val intent = Intent(context, activity).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 0, intent, 0)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(androidx.work.R.drawable.notification_template_icon_low_bg)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            /** 5. Send the notification **/
            NotificationManagerCompat
                .from(context)
                .notify(10000, notification)
        }

    }
}