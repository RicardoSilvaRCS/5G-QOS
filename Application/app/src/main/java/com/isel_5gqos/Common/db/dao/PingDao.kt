package com.isel_5gqos.Common.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isel_5gqos.Common.db.entities.Ping
import java.util.*

interface PingDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addNewPing(vararg pings:Ping)

    @Query("Select * from Ping where sessionId=:sessionId")
    fun getPingsBySessionId(sessionId:Int)
}