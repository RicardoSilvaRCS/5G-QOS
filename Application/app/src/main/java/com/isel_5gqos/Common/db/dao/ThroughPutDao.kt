package com.isel_5gqos.Common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isel_5gqos.Common.db.entities.ThroughPut
import com.isel_5gqos.dtos.ThroughPutDto
import java.util.*

@Dao
interface ThroughPutDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg throughPutDto: ThroughPut)

    @Query("Select * from ThroughPuts where sessionId = :session")
    fun get(session:String): LiveData<List<ThroughPut>>

}