package com.isel_5gqos.common.db.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

data class UserLogin (
    @Embedded val user: User,
    @Relation(
        parentColumn = "username",
        entityColumn = "user"
    )
    val login: Login
)