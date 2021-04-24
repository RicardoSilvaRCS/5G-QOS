package com.isel_5gqos.Common.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(
    tableName = "ThroughPuts",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("sessionId"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = [
        "regId"
    ]
)

class ThroughPut(
    val regId: String,
    val txResult: Long,
    val rxResult: Long,
    val sessionId: String,
    val timestamp: Timestamp
)
