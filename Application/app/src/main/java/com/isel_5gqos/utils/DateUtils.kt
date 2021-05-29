package com.isel_5gqos.utils

import java.text.SimpleDateFormat
import java.util.*

class DateUtils {
    companion object {
        fun formatDate(date: Date): String = SimpleDateFormat("dd-MMM-yyyy HH:mm").format(date)
    }

}