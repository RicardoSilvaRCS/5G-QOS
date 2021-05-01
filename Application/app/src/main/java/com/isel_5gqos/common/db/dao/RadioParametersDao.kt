package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isel_5gqos.common.db.entities.RadioParameters

@Dao
interface RadioParametersDao {

    @Query("Select * from RadioParameters where sessionId = :session")
    fun get(session: String): LiveData<List<RadioParameters>>

    @Query("Select * from RadioParameters where sessionId = :sessionId and isUpToDate = 0 LIMIT 5")
    fun getUpToDateRadioParameters(sessionId: String): LiveData<List<RadioParameters>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg radioParameter: RadioParameters)

    @Update
    fun updateRadioParameter(vararg radioParameters: RadioParameters)

    @Query("Update RadioParameters set isUpToDate = 0 where sessionId = :sessionId")
    fun invalidateRadioParameters(sessionId: String)

    @Delete()
    fun delete(radioParameter: RadioParameters)

    @Query("Delete from RadioParameters where sessionId = :session")
    fun deleteRadioParameter(session: String)

}