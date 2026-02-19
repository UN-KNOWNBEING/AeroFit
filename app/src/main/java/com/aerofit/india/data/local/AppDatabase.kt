package com.aerofit.india.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AqiEntity::class, UserEntity::class, DailyRecordEntity::class],
    version = 2, // Changed to version 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun aqiDao(): AqiDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aerofit_database"
                )
                    .fallbackToDestructiveMigration() // Safely upgrades the database
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}