package com.isel_5gqos.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DateUtils {
    companion object {
        fun formatDate(date: Date): String = SimpleDateFormat("dd-MMM-yyyy HH:mm").format(date)

        fun getDateIso8601Format() : String {
            val tz = TimeZone.getTimeZone("UTC")
            val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            df.setTimeZone(tz)
            return df.format(Date())
        }
    }

}