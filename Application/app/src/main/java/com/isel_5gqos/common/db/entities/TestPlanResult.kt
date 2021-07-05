package com.isel_5gqos.common.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "TestPlanResults",
    foreignKeys = [
        ForeignKey(
            entity = TestPlan::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("testPlanId"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = [
        "regId"
    ]
)
class TestPlanResult (
    val regId: String,
    @ColumnInfo(name = "testPlanId", index = true)
    val testPlanId: String,
    val testId : String,
    val result : String,
    val isReported : Boolean
)