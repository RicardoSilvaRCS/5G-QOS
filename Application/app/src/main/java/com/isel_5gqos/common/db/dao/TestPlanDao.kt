package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isel_5gqos.common.db.entities.TestPlan

@Dao
interface TestPlanDao {

    @Query("select * from TestPlans")
    fun getTestPlans () : LiveData<List<TestPlan>>

    @Query("Update TestPlans set testPlanState = :testPlantState where id = :testPlanId")
    fun updateTestPLan(testPlanId:String,testPlantState:String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg testPlan: TestPlan)


}