package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isel_5gqos.common.db.entities.Location
import com.isel_5gqos.common.db.entities.RadioParameters

@Dao
interface RadioParametersDao {

    @Query("Select * from RadioParameters where sessionId = :session")
    fun get(session: String): LiveData<List<RadioParameters>>

    @Query("Select * from RadioParameters where sessionId = :sessionId and isUpToDate = 1")
    fun getUpToDateRadioParameters(sessionId: String): LiveData<List<RadioParameters>>

    @Query("Select * from RadioParameters where sessionId = :sessionId and ( isServingCell = 1 or `no` = 1 )")
    fun getServingCells(sessionId: String): LiveData<List<RadioParameters>>

    @Query("Select * from RadioParameters where sessionId = :sessionId and ( isServingCell = 1 or `no` = 1 ) and isUpToDate = 1 order by timestamp desc")
    fun getServingCell(sessionId: String): LiveData<RadioParameters>

    @Query("Select * from Locations where sessionId=:sessionId limit(1)")
    fun getLastLocation(sessionId: String):LiveData<Location>

    @Query("Select * from RadioParameters")
    fun getAllRadioParameters():LiveData<List<RadioParameters>>

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