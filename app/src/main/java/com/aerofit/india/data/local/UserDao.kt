package com.aerofit.india.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    // --- LIFETIME USER PROFILE ---
    @Query("SELECT * FROM user_table WHERE id = 'user_1' LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: UserEntity)

    // --- DAILY HISTORY RECORD ---
    @Query("SELECT * FROM daily_history_table WHERE dateString = :date LIMIT 1")
    suspend fun getDailyRecord(date: String): DailyRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyRecord(record: DailyRecordEntity)

    @Query("SELECT * FROM daily_history_table ORDER BY dateString DESC")
    suspend fun getAllHistory(): List<DailyRecordEntity>
}