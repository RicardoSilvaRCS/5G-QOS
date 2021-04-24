package com.isel_5gqos.Common.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.isel_5gqos.Common.db.dao.*
import com.isel_5gqos.Common.db.entities.Ping
import com.isel_5gqos.Common.db.entities.Session
import com.isel_5gqos.Common.db.entities.ThroughPut
import com.isel_5gqos.Common.db.entities.User

@Database(
    entities = [Error::class, Ping::class, Session::class,ThroughPut::class, User::class],
    version = 1,
    exportSchema = false
)
abstract class QosDb:RoomDatabase() {
    abstract fun errorDao():ErrorDao
    abstract fun sessionDao():SessionDao
    abstract fun throughPutDao():ThroughPutDao
    abstract fun userDao():UserDao
    abstract fun pingDao():PingDao
}