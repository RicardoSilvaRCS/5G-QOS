package com.isel_5gqos.Common.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp
import java.util.*

@Entity(tableName = "Errors",primaryKeys = ["id"])
class Error (
    val id : String,
    val description: String,
    val timestamp: Timestamp
)