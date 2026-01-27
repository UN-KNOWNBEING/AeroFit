package com.aerofit.india.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AqiDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheAqi(entity: AqiEntity)

    @Query("SELECT * FROM aqi_cache WHERE tileId = :tileId")
    suspend fun getAqiForTile(tileId: String): AqiEntity?

    @Query("DELETE FROM aqi_cache WHERE lastUpdatedTimestamp < :threshold")
    suspend fun clearStaleData(threshold: Long)
}
