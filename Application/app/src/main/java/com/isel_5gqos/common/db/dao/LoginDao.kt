package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isel_5gqos.common.db.entities.Login

@Dao
interface LoginDao {

    @Query("Select * from Logins where user = :username")
    fun getToken(username: String): LiveData<List<Login>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserLogin(login : Login)

    @Query("Update Logins set token = :newToken where user = :username")
    fun updateToken(username : String, newToken: String)

}