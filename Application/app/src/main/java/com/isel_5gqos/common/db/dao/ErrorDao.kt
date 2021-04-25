package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isel_5gqos.common.db.entities.Error

@Dao
interface ErrorDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addNewError(vararg errorDto: Error)

    @Query("Select * from Errors where id=:id")
    fun getErrorById(id: String): LiveData<List<Error>>
}