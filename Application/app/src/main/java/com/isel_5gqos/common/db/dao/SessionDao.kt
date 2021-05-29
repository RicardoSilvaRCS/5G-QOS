package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isel_5gqos.common.db.entities.Session

@Dao
interface SessionDao {

    @Query("Select * from Sessions where id = :sessionId")
    fun get(sessionId: String): LiveData<List<Session>>

    @Query("Select * from Sessions where id <> '-1' order by beginDate desc limit(2)")
    fun getLastSession():LiveData<List<Session>>

    @Query("Select * from Sessions where endDate <> 0")
    fun getCompletedSessions():LiveData<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg session: Session)

    @Update
    fun updateSession(vararg session: Session)

    @Delete()
    fun delete(session: Session)

    @Query("Delete from Sessions where id = :sessionId")
    fun deleteSession(sessionId : String)
}