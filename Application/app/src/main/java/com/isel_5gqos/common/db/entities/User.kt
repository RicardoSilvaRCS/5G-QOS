package com.isel_5gqos.common.db.entities

import androidx.room.Entity

@Entity(tableName = "Users", primaryKeys = ["username"])

class User (
    val regId: String,
    val username: String,
    val timestamp: Long,
)

