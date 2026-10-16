package com.example.data.model

data class UserPreferences(
    val isAutomationEnabled: Boolean = true,
    val interval: ScheduleInterval = ScheduleInterval.HOUR_1,
    val target: WallpaperTarget = WallpaperTarget.HOME,
    val selectedCategories: Set<String> = setOf("Nature", "Mountains", "AMOLED Black", "Space"),
    val activeHoursEnabled: Boolean = false,
    val activeHoursStart: Int = 8, // 8:00 AM
    val activeHoursEnd: Int = 22, // 10:00 PM
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: Int = 23, // 11:00 PM
    val quietHoursEnd: Int = 7, // 7:00 AM
    val wifiOnly: Boolean = false,
    val requireCharging: Boolean = false,
    val pauseOnLowBattery: Boolean = true,
    val pauseOnPowerSaver: Boolean = true,
    val maxDailyAiGenerations: Int = 5,
    val aiGenerationsUsedToday: Int = 0,
    val maxDailyDownloads: Int = 20,
    val downloadsUsedToday: Int = 0,
    val maxCacheSizeMb: Int = 300,
    val deduplicationEnabled: Boolean = true,
    val allowReuseOldWhenExhausted: Boolean = true,
    val notifyOnWallpaperChange: Boolean = true,
    val isOnboardingCompleted: Boolean = false,
    val lastQuotaResetDate: String = ""
)

data class DiagnosticInfo(
    val isAutomationEnabled: Boolean,
    val scheduleIntervalName: String,
    val targetScreen: String,
    val nextEligibleExecution: String,
    val lastSuccessfulChange: String,
    val lastFailedChange: String,
    val cachedWallpapersCount: Int,
    val favoritesCount: Int,
    val storageUsedMb: Double,
    val isNetworkAvailable: Boolean,
    val isWifiConnected: Boolean,
    val isBatterySaverActive: Boolean,
    val isCharging: Boolean,
    val batteryPercent: Int,
    val aiQuotaRemaining: Int,
    val isNotificationPermissionGranted: Boolean,
    val recentErrors: List<String>
)
