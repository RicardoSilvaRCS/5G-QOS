package com.isel_5gqos.common.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "Pings",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("sessionId"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = [
        "sessionId"
    ]
)

class Ping(
    val id: String,
    val sessionId: String,
    val avg: Int,
    val max: Int,
    val min: Int,
    val nrOfPackets: Int,
    val url: String
)