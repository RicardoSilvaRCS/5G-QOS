package com.isel_5gqos.common.db.entities

import androidx.room.ColumnInfo

data class SystemDatabaseInfo(
    @ColumnInfo(name = "page_size", index = true)
    val page_size:Int?=null,
    val page_count:Int?=null
)