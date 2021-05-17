package com.isel_5gqos.common.db.entities

import androidx.room.Entity

/**Each mobile device will have only one record**/

@Entity(
    tableName = "MobileUnit",
    primaryKeys = [
        "mobileUnitId"
    ]
)
class MobileUnit (
    val mobileUnitId : Int,
    val password : String,
    val controlConnectionHref: String,
    val systemLogHref: String,
)