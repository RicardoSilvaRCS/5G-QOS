package com.isel_5gqos.common.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "Logins",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("username"),
            childColumns = arrayOf("user"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["user"])

class Login(
    val user: String,
    val token: String,
    val timestamp: Long
)



