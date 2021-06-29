package com.isel_5gqos.repositories

import android.database.Cursor
import com.isel_5gqos.QosApp

class SystemRepository {
    fun getDatabaseInfo():Pair<Long, Cursor>{
        val pageSize = QosApp.db.openHelper.readableDatabase.pageSize
        val cursor = QosApp.db.openHelper.readableDatabase.query("Select page_count from pragma_page_count")
        return Pair(pageSize,cursor)
    }
}