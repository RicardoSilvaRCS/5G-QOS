package com.isel_5gqos.common.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.isel_5gqos.common.db.dao.LocationDao
import com.isel_5gqos.common.db.dao.RadioParametersDao
import com.isel_5gqos.common.db.entities.Location
import com.isel_5gqos.common.db.entities.RadioParameters
import com.isel_5gqos.common.db.converters.Converters
import com.isel_5gqos.common.db.dao.*
import com.isel_5gqos.common.db.entities.*

@Database(
    entities = [Error::class, Ping::class, Session::class, ThroughPut::class, User::class, Location::class , RadioParameters::class, MobileUnit::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class QosDb : RoomDatabase() {
    abstract fun errorDao(): ErrorDao
    abstract fun sessionDao(): SessionDao
    abstract fun throughPutDao(): ThroughPutDao
    abstract fun userDao(): UserDao
    abstract fun mobileUnit():MobileUnitDao
    abstract fun pingDao(): PingDao
    abstract fun locationDao(): LocationDao
    abstract fun radioParametersDao():RadioParametersDao
    abstract fun systemInfoDao():SystemInfoDao
}