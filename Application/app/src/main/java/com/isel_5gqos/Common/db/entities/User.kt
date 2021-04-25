package com.isel_5gqos.Common.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp
import java.util.*

@Entity(tableName = "Users", primaryKeys = ["username"])

class User (
    val regId : String,
    val username : String,
    val token : String,
    val timestamp: Long
)

