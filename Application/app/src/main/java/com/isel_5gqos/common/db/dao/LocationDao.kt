package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isel_5gqos.common.db.entities.Location

@Dao
interface LocationDao {

    @Query("Select * from Locations where sessionId = :session")
    fun get(session: String): LiveData<List<Location>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg location: Location)

    @Update
    fun updateSession(vararg location: Location)

    @Delete()
    fun delete(location: Location)

    @Query("Delete from RadioParameters where sessionId = :session")
    fun deleteSessionData(session: String)

}