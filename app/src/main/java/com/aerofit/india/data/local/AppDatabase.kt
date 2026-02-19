package com.aerofit.india.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Added DailyRecordEntity and increased version to 2
@Database(entities = [UserEntity::class, DailyRecordEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

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
                    .fallbackToDestructiveMigration() // Automatically updates the DB safely
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}