package com.isel_5gqos.common.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

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
    val latitude : String,
    val longitude : String,
    @ColumnInfo(name = "sessionId", index = true)
    val sessionId: String,
    val timestamp: Long
)

