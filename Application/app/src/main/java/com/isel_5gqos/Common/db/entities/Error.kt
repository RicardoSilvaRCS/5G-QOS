package com.isel_5gqos.Common.db.entities

import androidx.room.Entity
import java.util.*

@Entity(tableName = "Errors",primaryKeys = ["id"])
class Error (
    val id : String,
    val description: String,
    val timestamp: Long
)