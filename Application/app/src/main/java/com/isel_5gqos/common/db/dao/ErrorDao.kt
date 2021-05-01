package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isel_5gqos.common.db.entities.Error

@Dao
interface ErrorDao {

    @Query("Select * from Errors")
    fun getAllErrors (): LiveData<List<Error>>

    @Query("Select * from Errors where regId=:id")
    fun getErrorById(id: String): LiveData<List<Error>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg errorDto: Error)

}