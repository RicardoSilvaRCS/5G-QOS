package com.isel_5gqos.Common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isel_5gqos.Common.db.entities.ThroughPut
import com.isel_5gqos.dtos.ThroughPutDto
import java.util.*

interface ThroughPutDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg throughPutDto: ThroughPutDto)

    @Query("Select * from ThroughPut where sessionId = :session")
    fun get(session:UUID): LiveData<List<ThroughPut>>

}