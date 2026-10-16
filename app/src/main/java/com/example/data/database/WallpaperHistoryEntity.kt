package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpaper_history")
data class WallpaperHistoryEntity(
    @PrimaryKey(autoGenerate = true) val historyId: Long = 0,
    val wallpaperId: String,
    val title: String,
    val source: String,
    val targetScreen: String,
    val success: Boolean,
    val errorMessage: String?,
    val timestamp: Long = System.currentTimeMillis()
)
