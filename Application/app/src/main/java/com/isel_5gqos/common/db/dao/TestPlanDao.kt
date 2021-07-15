package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isel_5gqos.common.db.entities.TestPlan

@Dao
interface TestPlanDao {

    @Query("select * from TestPlans")
    fun getTestPlans(): LiveData<List<TestPlan>>

    @Query("select * from TestPlans where testPlanState = 'FINISHED'")
    fun getFinishedTestPlans(): LiveData<List<TestPlan>>

    @Query("Update TestPlans set testPlanState = :testPlantState where id = :testPlanId")
    fun updateTestPLan(testPlanId: String, testPlantState: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg testPlan: TestPlan)

    @Query("delete from TestPlans where id = :testPlanId")
    fun deleteTestPlanById(testPlanId: String)


}