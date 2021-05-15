package com.isel_5gqos.common.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.isel_5gqos.common.db.entities.SystemDatabaseInfo

@Dao
interface SystemInfoDao {
    @Query("SELECT s.*, c.* FROM pragma_page_size as s JOIN pragma_page_count as c")
    fun getDatabaseSize():LiveData<SystemDatabaseInfo>
}