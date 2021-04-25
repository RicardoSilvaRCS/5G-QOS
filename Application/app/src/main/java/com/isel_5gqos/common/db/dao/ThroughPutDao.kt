package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isel_5gqos.common.db.entities.ThroughPut

@Dao
interface ThroughPutDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg throughPutDto: ThroughPut)

    @Query("Select * from ThroughPuts where sessionId = :session")
    fun get(session: String): LiveData<List<ThroughPut>>

}