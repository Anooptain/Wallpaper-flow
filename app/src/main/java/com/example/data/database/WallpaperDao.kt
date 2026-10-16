package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {
    @Query("SELECT * FROM wallpapers ORDER BY timestamp DESC")
    fun getAllWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE category = :category ORDER BY timestamp DESC")
    fun getWallpapersByCategory(category: String): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isExcluded = 0 AND (category IN (:categories) OR :includeAll = 1)")
    suspend fun getEligibleWallpapers(categories: List<String>, includeAll: Boolean): List<WallpaperEntity>

    @Query("SELECT * FROM wallpapers WHERE id = :id LIMIT 1")
    suspend fun getWallpaperById(id: String): WallpaperEntity?

    @Query("SELECT * FROM wallpapers WHERE hashSha256 = :hash LIMIT 1")
    suspend fun getWallpaperByHash(hash: String): WallpaperEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpaper(wallpaper: WallpaperEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpapers(wallpapers: List<WallpaperEntity>)

    @Query("UPDATE wallpapers SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE wallpapers SET isExcluded = :isExcluded WHERE id = :id")
    suspend fun updateExcluded(id: String, isExcluded: Boolean)

    @Query("UPDATE wallpapers SET lastAppliedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateLastApplied(id: String, timestamp: Long)

    @Query("DELETE FROM wallpapers WHERE id = :id")
    suspend fun deleteWallpaper(id: String)

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 0 ORDER BY timestamp ASC")
    suspend fun getNonFavoriteWallpapersOldestFirst(): List<WallpaperEntity>

    @Query("DELETE FROM wallpapers WHERE isFavorite = 0")
    suspend fun clearNonFavoriteWallpapers()

    @Query("SELECT COUNT(*) FROM wallpapers")
    suspend fun getWallpaperCount(): Int

    @Query("SELECT COUNT(*) FROM wallpapers WHERE isFavorite = 1")
    suspend fun getFavoriteCount(): Int

    // History queries
    @Insert
    suspend fun insertHistory(history: WallpaperHistoryEntity)

    @Query("SELECT * FROM wallpaper_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistoryFlow(limit: Int = 20): Flow<List<WallpaperHistoryEntity>>

    @Query("SELECT * FROM wallpaper_history WHERE success = 1 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSuccessfulHistory(): WallpaperHistoryEntity?

    @Query("SELECT * FROM wallpaper_history WHERE success = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastFailedHistory(): WallpaperHistoryEntity?

    @Query("SELECT * FROM wallpaper_history ORDER BY timestamp DESC LIMIT 10")
    suspend fun getRecentHistoryList(): List<WallpaperHistoryEntity>
}
