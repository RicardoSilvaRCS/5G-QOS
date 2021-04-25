package com.isel_5gqos.Common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isel_5gqos.Common.db.entities.Session
import com.isel_5gqos.Common.db.entities.ThroughPut
import java.util.*

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg session : Session)

    @Delete()
    fun delete(session: Session)

    @Query("Select * from Sessions where id = :session")
    fun get(session: String): LiveData<List<Session>>
}