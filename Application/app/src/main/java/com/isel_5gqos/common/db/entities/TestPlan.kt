package com.isel_5gqos.common.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "TestPlans",
    primaryKeys = [
        "id"
    ]
)
class TestPlan(
    val id: String,
    val name: String,
    val startDate : String,
    val testPlanState : String,
    val timestamp: Long,
)