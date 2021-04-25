package com.isel_5gqos.Common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isel_5gqos.Common.db.entities.Ping
import java.util.*

@Dao
interface PingDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addNewPing(vararg pings:Ping)

    @Query("Select * from Pings where sessionId=:sessionId")
    fun getPingsBySessionId(sessionId:String) : LiveData<List<Ping>>
}