package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.database.AppDatabase
import com.example.data.database.WallpaperEntity
import com.example.data.database.WallpaperHistoryEntity
import com.example.data.model.DiagnosticInfo
import com.example.data.model.UserPreferences
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperSource
import com.example.data.model.WallpaperTarget
import com.example.data.network.providers.AiGenerationResult
import com.example.data.network.providers.AiWallpaperEngine
import com.example.data.network.providers.CuratedOnlineProvider
import com.example.data.network.providers.LocalWallpaperManager
import com.example.data.preferences.UserPreferencesRepository
import com.example.notifications.NotificationHelper
import com.example.utils.BatteryUtils
import com.example.utils.ImageStorageManager
import com.example.utils.NetworkUtils
import com.example.wallpaper.WallpaperApplyResult
import com.example.wallpaper.WallpaperManagerHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WallpaperRepository(private val context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val dao = database.wallpaperDao()
    val preferencesRepository = UserPreferencesRepository(context)

    private val onlineProvider = CuratedOnlineProvider()
    private val localManager = LocalWallpaperManager(context)
    private val aiEngine = AiWallpaperEngine(context, preferencesRepository)
    private val wallpaperManagerHelper = WallpaperManagerHelper(context)
    private val imageStorageManager = ImageStorageManager(context)

    val allWallpapers: Flow<List<Wallpaper>> = dao.getAllWallpapers().map { entities ->
        entities.map { it.toDomain() }
    }

    val favoriteWallpapers: Flow<List<Wallpaper>> = dao.getFavoriteWallpapers().map { entities ->
        entities.map { it.toDomain() }
    }

    val recentHistory: Flow<List<WallpaperHistoryEntity>> = dao.getRecentHistoryFlow(20)

    val userPreferences: Flow<UserPreferences> = preferencesRepository.userPreferencesFlow

    suspend fun initialize() = withContext(Dispatchers.IO) {
        localManager.seedStarterWallpapersIfEmpty()
    }

    suspend fun toggleFavorite(id: String, isCurrentlyFavorite: Boolean) = withContext(Dispatchers.IO) {
        dao.updateFavorite(id, !isCurrentlyFavorite)
    }

    suspend fun deleteWallpaper(id: String) = withContext(Dispatchers.IO) {
        val entity = dao.getWallpaperById(id)
        if (entity?.localFilePath != null) {
            val file = java.io.File(entity.localFilePath)
            if (file.exists()) file.delete()
        }
        dao.deleteWallpaper(id)
    }

    suspend fun applyWallpaper(wallpaper: Wallpaper, target: WallpaperTarget): WallpaperApplyResult = withContext(Dispatchers.IO) {
        val result = wallpaperManagerHelper.applyWallpaper(wallpaper, target)
        if (result is WallpaperApplyResult.Success) {
            val prefs = userPreferences.first()
            if (prefs.notifyOnWallpaperChange) {
                NotificationHelper.notifyWallpaperChanged(context, wallpaper.title, target, wallpaper.author)
            }
        }
        result
    }

    suspend fun changeWallpaperNow(): WallpaperApplyResult = withContext(Dispatchers.IO) {
        val prefs = userPreferences.first()
        val categories = prefs.selectedCategories.toList()
        val eligibleEntities = dao.getEligibleWallpapers(categories, categories.isEmpty())

        if (eligibleEntities.isEmpty()) {
            return@withContext WallpaperApplyResult.Failure(
                null,
                "No eligible cached wallpapers found. Please explore or add wallpapers."
            )
        }

        // Rotate: Pick eligible wallpaper with oldest lastAppliedTimestamp
        val selectedEntity = eligibleEntities.minByOrNull { it.lastAppliedTimestamp ?: 0L }
            ?: eligibleEntities.first()

        applyWallpaper(selectedEntity.toDomain(), prefs.target)
    }

    suspend fun importPhoto(uri: Uri, title: String, category: String): Result<Wallpaper> = withContext(Dispatchers.IO) {
        localManager.importUserSelectedPhoto(uri, title, category)
    }

    suspend fun generateAiWallpaper(
        prompt: String,
        style: String,
        primaryHex: String,
        secondaryHex: String
    ): AiGenerationResult = withContext(Dispatchers.IO) {
        aiEngine.generateWallpaper(prompt, style, primaryHex, secondaryHex)
    }

    suspend fun fetchOnlineWallpapers(category: String, query: String? = null): Result<List<Wallpaper>> = withContext(Dispatchers.IO) {
        onlineProvider.fetchWallpapers(category, 1, query)
    }

    suspend fun downloadAndCacheWallpaper(wallpaper: Wallpaper): Result<Wallpaper> = withContext(Dispatchers.IO) {
        try {
            // Check if already in DB
            val existing = dao.getWallpaperById(wallpaper.id)
            if (existing != null && existing.localFilePath != null && java.io.File(existing.localFilePath).exists()) {
                return@withContext Result.success(existing.toDomain())
            }

            // Download bytes using OkHttp or URL connection
            val url = java.net.URL(wallpaper.imageUrl)
            val connection = url.openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 15000
            val inputStream = connection.getInputStream()

            val fileName = "${wallpaper.id}.jpg"
            val file = imageStorageManager.saveImageToFile(fileName, inputStream)
            val hash = imageStorageManager.calculateSha256(file)

            val cached = wallpaper.copy(
                localFilePath = file.absolutePath,
                source = WallpaperSource.CACHED,
                hashSha256 = hash
            )
            dao.insertWallpaper(WallpaperEntity.fromDomain(cached))
            preferencesRepository.incrementDownloadsUsed()
            Result.success(cached)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearCache(): Int = withContext(Dispatchers.IO) {
        imageStorageManager.cleanCachePreservingFavorites(database)
    }

    suspend fun getDiagnostics(): DiagnosticInfo = withContext(Dispatchers.IO) {
        val prefs = userPreferences.first()
        val count = dao.getWallpaperCount()
        val favorites = dao.getFavoriteCount()
        val storageMb = imageStorageManager.getStorageUsedMb()
        val isNet = NetworkUtils.isNetworkAvailable(context)
        val isWifi = NetworkUtils.isWifiConnected(context)
        val isBattSaver = BatteryUtils.isPowerSaveMode(context)
        val isCharging = BatteryUtils.isCharging(context)
        val battLevel = BatteryUtils.getBatteryLevel(context)
        val lastSuccess = dao.getLastSuccessfulHistory()
        val lastFailed = dao.getLastFailedHistory()
        val recentErrors = dao.getRecentHistoryList()
            .filter { !it.success && it.errorMessage != null }
            .map { it.errorMessage!! }

        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())

        val lastSuccessStr = lastSuccess?.let { dateFormat.format(Date(it.timestamp)) } ?: "None yet"
        val lastFailedStr = lastFailed?.let { "${dateFormat.format(Date(it.timestamp))} (${it.errorMessage})" } ?: "None"

        val nextEligible = if (prefs.isAutomationEnabled) {
            "In approx ${prefs.interval.minutes} min (inexact WorkManager execution window)"
        } else {
            "Automation Paused"
        }

        DiagnosticInfo(
            isAutomationEnabled = prefs.isAutomationEnabled,
            scheduleIntervalName = prefs.interval.displayName,
            targetScreen = prefs.target.title,
            nextEligibleExecution = nextEligible,
            lastSuccessfulChange = lastSuccessStr,
            lastFailedChange = lastFailedStr,
            cachedWallpapersCount = count,
            favoritesCount = favorites,
            storageUsedMb = storageMb,
            isNetworkAvailable = isNet,
            isWifiConnected = isWifi,
            isBatterySaverActive = isBattSaver,
            isCharging = isCharging,
            batteryPercent = battLevel,
            aiQuotaRemaining = (prefs.maxDailyAiGenerations - prefs.aiGenerationsUsedToday).coerceAtLeast(0),
            isNotificationPermissionGranted = NotificationHelper.hasNotificationPermission(context),
            recentErrors = recentErrors
        )
    }
}
