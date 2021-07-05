package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isel_5gqos.common.db.entities.TestPlanResult

@Dao
interface TestPlanResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg testPlanResult: TestPlanResult)

    @Query("update TestPlanResults set isReported = :isReported where testPlanId = :testPlanId and testId = :testId ")
    fun updateIsReported (testPlanId: String, testId : String ,isReported : Boolean)

    @Query("Select * from TestPlanResults where testPlanId = :testPlanId")
    fun getTestPlanResults(testPlanId: String) : LiveData<List<TestPlanResult>>

    @Query("Select * from TestPlanResults where isReported = 0")
    fun getUnreportedTests() : LiveData<List<TestPlanResult>>
}