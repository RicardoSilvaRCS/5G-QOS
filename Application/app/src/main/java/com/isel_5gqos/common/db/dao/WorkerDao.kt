package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isel_5gqos.common.db.entities.Worker

@Dao
interface WorkerDao {

    @Query("Select * from Workers where id=:id")
    fun getWorkersByTag(id:String):LiveData<List<Worker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWorker(vararg worker: Worker)

    @Query("Update Workers set finished = 1 where tag=:tag")
    fun signalWorkerByTagToFinish(tag:String)
}