package com.isel_5gqos.common.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

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
    val id: String = "",
    val sessionName: String = "",
    @ColumnInfo(name = "user", index = true)
    val user: String = "",
    val beginDate: Long = 0L,
    var endDate: Long = 0L
)