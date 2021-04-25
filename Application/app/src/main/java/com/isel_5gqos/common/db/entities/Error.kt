package com.isel_5gqos.common.db.entities

import androidx.room.Entity

@Entity(tableName = "Errors", primaryKeys = ["id"])
class Error(
    val id: String,
    val description: String,
    val timestamp: Long
)