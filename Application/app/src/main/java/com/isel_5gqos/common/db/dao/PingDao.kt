package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isel_5gqos.common.db.entities.Ping

@Dao
interface PingDao {

    @Query("Select * from Pings where sessionId=:sessionId")
    fun getPingsBySessionId(sessionId: String): LiveData<List<Ping>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addNewPing(vararg pings: Ping)

}