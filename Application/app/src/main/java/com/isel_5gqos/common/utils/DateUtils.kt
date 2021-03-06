package com.isel_5gqos.common.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DateUtils {
    companion object {
        fun formatDate(date: Date): String = SimpleDateFormat("dd-MMM-yyyy HH:mm").format(date)

        fun getDateByFormat(format:String = "dd-MMM-yyyy HH:mm") = SimpleDateFormat(format).format(Date())


        fun getDateIso8601Format() : String {
            val tz = TimeZone.getTimeZone("UTC")
            val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            df.setTimeZone(tz)
            return df.format(Date())
        }
    }

}