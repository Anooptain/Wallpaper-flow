package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ScheduleInterval
import com.example.data.model.UserPreferences
import com.example.data.model.WallpaperTarget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_AUTOMATION_ENABLED = booleanPreferencesKey("is_automation_enabled")
        val INTERVAL = stringPreferencesKey("schedule_interval")
        val TARGET = stringPreferencesKey("wallpaper_target")
        val SELECTED_CATEGORIES = stringSetPreferencesKey("selected_categories")
        val ACTIVE_HOURS_ENABLED = booleanPreferencesKey("active_hours_enabled")
        val ACTIVE_HOURS_START = intPreferencesKey("active_hours_start")
        val ACTIVE_HOURS_END = intPreferencesKey("active_hours_end")
        val QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val QUIET_HOURS_START = intPreferencesKey("quiet_hours_start")
        val QUIET_HOURS_END = intPreferencesKey("quiet_hours_end")
        val WIFI_ONLY = booleanPreferencesKey("wifi_only")
        val REQUIRE_CHARGING = booleanPreferencesKey("require_charging")
        val PAUSE_ON_LOW_BATTERY = booleanPreferencesKey("pause_on_low_battery")
        val PAUSE_ON_POWER_SAVER = booleanPreferencesKey("pause_on_power_saver")
        val MAX_DAILY_AI_GENERATIONS = intPreferencesKey("max_daily_ai_generations")
        val AI_GENERATIONS_USED_TODAY = intPreferencesKey("ai_generations_used_today")
        val MAX_DAILY_DOWNLOADS = intPreferencesKey("max_daily_downloads")
        val DOWNLOADS_USED_TODAY = intPreferencesKey("downloads_used_today")
        val MAX_CACHE_SIZE_MB = intPreferencesKey("max_cache_size_mb")
        val DEDUPLICATION_ENABLED = booleanPreferencesKey("deduplication_enabled")
        val ALLOW_REUSE_OLD = booleanPreferencesKey("allow_reuse_old")
        val NOTIFY_ON_CHANGE = booleanPreferencesKey("notify_on_change")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LAST_QUOTA_RESET_DATE = stringPreferencesKey("last_quota_reset_date")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val savedDate = preferences[PreferencesKeys.LAST_QUOTA_RESET_DATE] ?: ""
        val isNewDay = savedDate != todayStr

        val intervalName = preferences[PreferencesKeys.INTERVAL] ?: ScheduleInterval.HOUR_1.name
        val interval = try {
            ScheduleInterval.valueOf(intervalName)
        } catch (e: Exception) {
            ScheduleInterval.HOUR_1
        }

        val targetName = preferences[PreferencesKeys.TARGET] ?: WallpaperTarget.HOME.name
        val target = try {
            WallpaperTarget.valueOf(targetName)
        } catch (e: Exception) {
            WallpaperTarget.HOME
        }

        UserPreferences(
            isAutomationEnabled = preferences[PreferencesKeys.IS_AUTOMATION_ENABLED] ?: true,
            interval = interval,
            target = target,
            selectedCategories = preferences[PreferencesKeys.SELECTED_CATEGORIES]
                ?: setOf("Nature", "Mountains", "AMOLED Black", "Space"),
            activeHoursEnabled = preferences[PreferencesKeys.ACTIVE_HOURS_ENABLED] ?: false,
            activeHoursStart = preferences[PreferencesKeys.ACTIVE_HOURS_START] ?: 8,
            activeHoursEnd = preferences[PreferencesKeys.ACTIVE_HOURS_END] ?: 22,
            quietHoursEnabled = preferences[PreferencesKeys.QUIET_HOURS_ENABLED] ?: false,
            quietHoursStart = preferences[PreferencesKeys.QUIET_HOURS_START] ?: 23,
            quietHoursEnd = preferences[PreferencesKeys.QUIET_HOURS_END] ?: 7,
            wifiOnly = preferences[PreferencesKeys.WIFI_ONLY] ?: false,
            requireCharging = preferences[PreferencesKeys.REQUIRE_CHARGING] ?: false,
            pauseOnLowBattery = preferences[PreferencesKeys.PAUSE_ON_LOW_BATTERY] ?: true,
            pauseOnPowerSaver = preferences[PreferencesKeys.PAUSE_ON_POWER_SAVER] ?: true,
            maxDailyAiGenerations = preferences[PreferencesKeys.MAX_DAILY_AI_GENERATIONS] ?: 5,
            aiGenerationsUsedToday = if (isNewDay) 0 else (preferences[PreferencesKeys.AI_GENERATIONS_USED_TODAY] ?: 0),
            maxDailyDownloads = preferences[PreferencesKeys.MAX_DAILY_DOWNLOADS] ?: 20,
            downloadsUsedToday = if (isNewDay) 0 else (preferences[PreferencesKeys.DOWNLOADS_USED_TODAY] ?: 0),
            maxCacheSizeMb = preferences[PreferencesKeys.MAX_CACHE_SIZE_MB] ?: 300,
            deduplicationEnabled = preferences[PreferencesKeys.DEDUPLICATION_ENABLED] ?: true,
            allowReuseOldWhenExhausted = preferences[PreferencesKeys.ALLOW_REUSE_OLD] ?: true,
            notifyOnWallpaperChange = preferences[PreferencesKeys.NOTIFY_ON_CHANGE] ?: true,
            isOnboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
            lastQuotaResetDate = savedDate
        )
    }

    suspend fun setAutomationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_AUTOMATION_ENABLED] = enabled
        }
    }

    suspend fun setInterval(interval: ScheduleInterval) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INTERVAL] = interval.name
        }
    }

    suspend fun setTarget(target: WallpaperTarget) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TARGET] = target.name
        }
    }

    suspend fun setSelectedCategories(categories: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_CATEGORIES] = categories
        }
    }

    suspend fun setActiveHours(enabled: Boolean, start: Int, end: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_HOURS_ENABLED] = enabled
            preferences[PreferencesKeys.ACTIVE_HOURS_START] = start
            preferences[PreferencesKeys.ACTIVE_HOURS_END] = end
        }
    }

    suspend fun setQuietHours(enabled: Boolean, start: Int, end: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUIET_HOURS_ENABLED] = enabled
            preferences[PreferencesKeys.QUIET_HOURS_START] = start
            preferences[PreferencesKeys.QUIET_HOURS_END] = end
        }
    }

    suspend fun setWifiOnly(wifiOnly: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIFI_ONLY] = wifiOnly
        }
    }

    suspend fun setRequireCharging(requireCharging: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REQUIRE_CHARGING] = requireCharging
        }
    }

    suspend fun setPauseOnLowBattery(pause: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PAUSE_ON_LOW_BATTERY] = pause
        }
    }

    suspend fun setPauseOnPowerSaver(pause: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PAUSE_ON_POWER_SAVER] = pause
        }
    }

    suspend fun setNotifyOnChange(notify: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFY_ON_CHANGE] = notify
        }
    }

    suspend fun setMaxCacheSizeMb(sizeMb: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAX_CACHE_SIZE_MB] = sizeMb
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun incrementAiGenerationsUsed() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        context.dataStore.edit { preferences ->
            val savedDate = preferences[PreferencesKeys.LAST_QUOTA_RESET_DATE] ?: ""
            if (savedDate != todayStr) {
                preferences[PreferencesKeys.LAST_QUOTA_RESET_DATE] = todayStr
                preferences[PreferencesKeys.AI_GENERATIONS_USED_TODAY] = 1
                preferences[PreferencesKeys.DOWNLOADS_USED_TODAY] = 0
            } else {
                val current = preferences[PreferencesKeys.AI_GENERATIONS_USED_TODAY] ?: 0
                preferences[PreferencesKeys.AI_GENERATIONS_USED_TODAY] = current + 1
            }
        }
    }

    suspend fun incrementDownloadsUsed() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        context.dataStore.edit { preferences ->
            val savedDate = preferences[PreferencesKeys.LAST_QUOTA_RESET_DATE] ?: ""
            if (savedDate != todayStr) {
                preferences[PreferencesKeys.LAST_QUOTA_RESET_DATE] = todayStr
                preferences[PreferencesKeys.DOWNLOADS_USED_TODAY] = 1
                preferences[PreferencesKeys.AI_GENERATIONS_USED_TODAY] = 0
            } else {
                val current = preferences[PreferencesKeys.DOWNLOADS_USED_TODAY] ?: 0
                preferences[PreferencesKeys.DOWNLOADS_USED_TODAY] = current + 1
            }
        }
    }
}
