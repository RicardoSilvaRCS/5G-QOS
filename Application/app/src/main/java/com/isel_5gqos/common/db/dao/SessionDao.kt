package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isel_5gqos.common.db.entities.Session

@Dao
interface SessionDao {

    @Query("Select * from Sessions where id = :session")
    fun get(session: String): LiveData<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg session: Session)

    @Update
    fun updateSession(vararg session: Session)

    @Delete()
    fun delete(session: Session)

}