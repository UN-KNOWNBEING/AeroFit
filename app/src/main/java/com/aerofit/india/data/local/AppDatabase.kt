package com.aerofit.india.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AqiEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun aqiDao(): AqiDao
}
