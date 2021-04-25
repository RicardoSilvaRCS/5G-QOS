package com.isel_5gqos.Common.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.sql.Timestamp
import java.util.*

@Entity(
    tableName = "Sessions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("username"),
            childColumns = arrayOf("user"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = [
        "id"
    ]
)

class Session(
    val id: String,
    val sessionName: String,
    @ColumnInfo(name= "user", index = true)
    val user: String,
    val beginDate: Long,
    val endDate: Long
)