package com.isel_5gqos.common.db.entities

import androidx.room.Entity

@Entity(tableName = "Errors", primaryKeys = ["regId"])
class Error(
    val regId: String,
    val description: String,
    val timestamp: Long
)