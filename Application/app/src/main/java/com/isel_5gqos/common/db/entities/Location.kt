package com.isel_5gqos.common.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "Locations",
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

class Location (
    val regId: String,
    val networkOperatorName:String,
    val latitude:Double,
    val longitude:Double,
    @ColumnInfo(name = "sessionId", index = true)
    val sessionId: String,
    val timestamp: Long,
)