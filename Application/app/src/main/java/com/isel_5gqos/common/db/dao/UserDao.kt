package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isel_5gqos.common.db.entities.User
import com.isel_5gqos.common.db.entities.UserLogin

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg throughPutDto: User)

    @Query("Select * from Users where loggedOut = 0 order by timestamp desc limit(1)")
    fun getLastLoggedUser () : LiveData<User>

    @Transaction
    @Query("Select * from users where loggedOut = 0 order by timestamp desc limit(1)")
    fun getToken(): LiveData<UserLogin>

}