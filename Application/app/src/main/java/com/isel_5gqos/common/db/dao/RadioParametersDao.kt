package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isel_5gqos.common.db.entities.RadioParameters

@Dao
interface RadioParametersDao {

    @Query("Select * from RadioParameters where sessionId = :session")
    fun get(session: String): LiveData<List<RadioParameters>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg radioParameter: RadioParameters)

    @Update
    fun updateSession(vararg radioParameters: RadioParameters)

    @Delete()
    fun delete(radioParameter: RadioParameters)

    @Query("Delete from RadioParameters where sessionId = :session")
    fun deleteSessionData(session: String)

}