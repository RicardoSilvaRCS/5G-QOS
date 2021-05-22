package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isel_5gqos.common.db.entities.User

@Dao
interface UserDao {

    @Query("Select * from Users where username = :username")
    fun getToken(username: String): LiveData<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg throughPutDto: User)

    @Query("Update Users set token = :newToken where token = :oldToken")
    fun updateToken(oldToken : String, newToken: String)

    @Query("Select * from Users where loggedOut = 0 order by timestamp desc limit(1)")
    fun getLoggedUser () : LiveData<User>

}