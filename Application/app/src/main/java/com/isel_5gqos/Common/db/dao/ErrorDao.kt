package com.isel_5gqos.Common.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isel_5gqos.dtos.ErrorDto
import java.util.*


interface ErrorDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addNewError(vararg errorDto:ErrorDto)

    @Query("Select * from Errors where id=:id")
    fun getErrorById(id: UUID)
}