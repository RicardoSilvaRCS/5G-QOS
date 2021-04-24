package com.isel_5gqos.Common.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Duration
import java.util.*

@Entity(tableName = "Sessions", foreignKeys = [
    ForeignKey(
        entity = User::class,
        parentColumns = arrayOf("username"),
        childColumns = arrayOf("username"),
        onDelete = ForeignKey.CASCADE
    )
])

class Session (
    @PrimaryKey
    val id : UUID,
    val sessionName : String,
    val username : String,
    val beginDate : Date,
    val endDate: Date
)