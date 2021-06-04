package com.isel_5gqos.common.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "RadioParameters",
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

class RadioParameters (
    val regId: String,
    val no : Int,
    val tech: String,
    val arfcn: Int,
    val rssi: Int,
    val rsrp: Int,
    val cId: Int,
    val psc: Int,
    val pci: Int,
    val rssnr: Int,
    val rsrq: Int,
    val netDataType: String,
    val isServingCell : Boolean,
    val numbOfCellsWithSameTechAsServing : Int,
    val latitude : String,
    val longitude : String,
    @ColumnInfo(name = "sessionId", index = true)
    val sessionId: String,
    val timestamp: Long,
    val isUpToDate:Boolean
)
