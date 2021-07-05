package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isel_5gqos.common.db.entities.TestPlan

@Dao
interface TestPlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg testPlan: TestPlan)

    @Query("select * from TestPlans")
    fun getTestPlans () : LiveData<List<TestPlan>>
}