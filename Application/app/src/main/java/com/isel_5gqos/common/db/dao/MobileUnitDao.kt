package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isel_5gqos.common.db.entities.MobileUnit

@Dao
interface MobileUnitDao {

    @Query("Select * from MobileUnit limit 1")
    fun getMobileUnitSettings (): LiveData<MobileUnit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMobileUnitSetting(vararg mobileUnit: MobileUnit)
}