package com.example.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import com.example.data.database.AppDatabase
import com.example.data.database.WallpaperHistoryEntity
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperTarget
import com.example.utils.ImageStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

sealed class WallpaperApplyResult {
    data class Success(val wallpaper: Wallpaper, val target: WallpaperTarget) : WallpaperApplyResult()
    data class Failure(val wallpaper: Wallpaper?, val errorMessage: String, val throwable: Throwable? = null) : WallpaperApplyResult()
}

class WallpaperManagerHelper(private val context: Context) {

    private val wallpaperManager: WallpaperManager by lazy {
        WallpaperManager.getInstance(context)
    }
    private val imageStorageManager = ImageStorageManager(context)
    private val database = AppDatabase.getInstance(context)

    suspend fun applyWallpaper(
        wallpaper: Wallpaper,
        target: WallpaperTarget
    ): WallpaperApplyResult = withContext(Dispatchers.IO) {
        val historyDao = database.wallpaperDao()

        // 1. Locate the file
        val file = if (wallpaper.localFilePath != null) {
            File(wallpaper.localFilePath)
        } else {
            null
        }

        if (file == null || !file.exists()) {
            val errorMsg = "Wallpaper image file not found locally: ${wallpaper.title}"
            logHistory(wallpaper.id, wallpaper.title, wallpaper.source.name, target.name, false, errorMsg)
            return@withContext WallpaperApplyResult.Failure(wallpaper, errorMsg)
        }

        // 2. Decode the bitmap safely with memory guard
        val bitmap = imageStorageManager.decodeSampledBitmapFromFile(file)
        if (bitmap == null) {
            val errorMsg = "Failed to decode image bitmap (corrupted file or unsupported format)"
            logHistory(wallpaper.id, wallpaper.title, wallpaper.source.name, target.name, false, errorMsg)
            return@withContext WallpaperApplyResult.Failure(wallpaper, errorMsg)
        }

        // 3. Apply via WallpaperManager using appropriate flags
        try {
            val success = applyBitmapToWallpaperManager(bitmap, target)
            if (success) {
                // Update last applied in DB
                historyDao.updateLastApplied(wallpaper.id, System.currentTimeMillis())
                logHistory(wallpaper.id, wallpaper.title, wallpaper.source.name, target.name, true, null)
                WallpaperApplyResult.Success(wallpaper, target)
            } else {
                val errorMsg = "WallpaperManager returned failure or lock screen not supported on this device"
                logHistory(wallpaper.id, wallpaper.title, wallpaper.source.name, target.name, false, errorMsg)
                WallpaperApplyResult.Failure(wallpaper, errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Unknown WallpaperManager exception"
            logHistory(wallpaper.id, wallpaper.title, wallpaper.source.name, target.name, false, errorMsg)
            WallpaperApplyResult.Failure(wallpaper, errorMsg, e)
        } finally {
            // Bitmap is managed by GC, but recycle if needed
        }
    }

    private fun applyBitmapToWallpaperManager(bitmap: Bitmap, target: WallpaperTarget): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when (target) {
                    WallpaperTarget.HOME -> {
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    }
                    WallpaperTarget.LOCK -> {
                        if (wallpaperManager.isSetWallpaperAllowed) {
                            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                        } else {
                            // Fallback if lock screen change is restricted
                            wallpaperManager.setBitmap(bitmap)
                        }
                    }
                    WallpaperTarget.BOTH -> {
                        wallpaperManager.setBitmap(
                            bitmap,
                            null,
                            true,
                            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                        )
                    }
                }
            } else {
                wallpaperManager.setBitmap(bitmap)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun logHistory(
        wallpaperId: String,
        title: String,
        source: String,
        targetScreen: String,
        success: Boolean,
        errorMessage: String?
    ) {
        try {
            database.wallpaperDao().insertHistory(
                WallpaperHistoryEntity(
                    wallpaperId = wallpaperId,
                    title = title,
                    source = source,
                    targetScreen = targetScreen,
                    success = success,
                    errorMessage = errorMessage,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            // Ignore history logging errors
        }
    }
}
