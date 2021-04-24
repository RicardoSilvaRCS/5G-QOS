package com.isel_5gqos.Common.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.sql.Timestamp
import java.util.*

@Entity(tableName = "ThroughPut", foreignKeys = [
    ForeignKey(
        entity = Session::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("sessionId"),
        onDelete = ForeignKey.CASCADE
    )
])

class ThroughPut(
    @PrimaryKey
    val regId: UUID,
    val txResult: Long,
    val rxResult: Long,
    val sessionId: UUID,
    val timestamp: Timestamp
)

