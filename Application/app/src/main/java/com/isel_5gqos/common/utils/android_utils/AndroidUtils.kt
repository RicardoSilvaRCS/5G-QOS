package com.isel_5gqos.common.utils.android_utils

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.isel_5gqos.R
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

        fun makeLoadingDialog(context: Context,message: String):AlertDialog{
            val inflater = LayoutInflater.from(context)
            val inflatedView = inflater.inflate(R.layout.dialog_loading,null)
            val textView = inflatedView.findViewById<TextView>(R.id.txt_progress_loading)
            textView.text = message
            return AlertDialog.Builder(context).setView(inflatedView).create()
        }

        fun setPreferences (key: String?, value: String?, context: Context?) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = preferences.edit()
            editor.putString(key, value)
            editor.apply()
        }

        fun getPreferences(key: String?, context: Context?): String? {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getString(key, "")
        }
    }
}