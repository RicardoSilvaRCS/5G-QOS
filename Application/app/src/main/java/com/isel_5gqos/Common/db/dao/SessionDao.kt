package com.isel_5gqos.Common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isel_5gqos.Common.db.entities.ThroughPut
import com.isel_5gqos.dtos.SessionDto
import com.isel_5gqos.dtos.ThroughPutDto
import java.util.*

interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg sessionDto : SessionDto)

    @Delete()
    fun delete(session: SessionDto)

    @Query("Select * from Sessions where id = :session")
    fun get(session: UUID): LiveData<List<ThroughPut>>
}