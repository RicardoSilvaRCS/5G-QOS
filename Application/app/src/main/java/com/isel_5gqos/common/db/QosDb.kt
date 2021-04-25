package com.isel_5gqos.common.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.isel_5gqos.common.db.converters.Converters
import com.isel_5gqos.common.db.dao.*
import com.isel_5gqos.common.db.entities.*

@Database(
    entities = [Error::class, Ping::class, Session::class, ThroughPut::class, User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class QosDb : RoomDatabase() {
    abstract fun errorDao(): ErrorDao
    abstract fun sessionDao(): SessionDao
    abstract fun throughPutDao(): ThroughPutDao
    abstract fun userDao(): UserDao
    abstract fun pingDao(): PingDao
}