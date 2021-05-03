package com.isel_5gqos.common.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity


@Entity(tableName = "Workers",primaryKeys=["id"])
class Worker (
    val id:String,
    @ColumnInfo(name = "tag", index = true)
    val tag:String,
    val finished:Boolean
)