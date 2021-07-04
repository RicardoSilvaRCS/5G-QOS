package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isel_5gqos.common.db.entities.Login
import com.isel_5gqos.common.db.entities.User
import com.isel_5gqos.common.db.entities.UserLogin

@Dao
interface LoginDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserLogin(login : Login)

    @Query("Update Logins set token = :newToken where user = :username")
    fun updateToken(username : String, newToken: String)

    @Query("Select * from Logins order by timestamp desc limit 1")
    fun getLastLoggedUser () : LiveData<Login>

    @Query("delete from Logins where user = :username")
    fun logoutActiveUser(username : String)

    @Query("Select * from logins order by timestamp desc limit(1)")
    fun getToken(): LiveData<Login>
}