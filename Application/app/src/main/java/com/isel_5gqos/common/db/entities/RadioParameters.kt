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
    val regId: String = "",
    val no : Int = -1,
    val tech: String = "",
    val arfcn: Int? = null,
    val rssi: Int? = null,
    val rsrp: Int? = null,
    val cId: Int? = null,
    val psc: Int? = null,
    val pci: Int? = null,
    val rssnr: Int? = null,
    val rsrq: Int? = null,
    val netDataType: String = "",
    val isServingCell : Boolean = false,
    val numbOfCellsWithSameTechAsServing : Int? = null,
    var latitude : String = "",
    var longitude : String = "",
    @ColumnInfo(name = "sessionId", index = true)
    val sessionId: String = "",
    val timestamp: Long? = null,
    val isUpToDate:Boolean = false
)
