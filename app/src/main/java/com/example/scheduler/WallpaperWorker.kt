package com.example.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.WallpaperRepository
import com.example.utils.BatteryUtils
import com.example.wallpaper.WallpaperApplyResult
import kotlinx.coroutines.flow.first
import java.util.Calendar

class WallpaperWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = WallpaperRepository(applicationContext)
        val prefs = repository.userPreferences.first()

        // 1. Check if automation is active
        if (!prefs.isAutomationEnabled) {
            return Result.success()
        }

        // 2. Check Quiet Hours
        if (prefs.quietHoursEnabled) {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isQuiet = if (prefs.quietHoursStart > prefs.quietHoursEnd) {
                // e.g. 23:00 to 07:00
                currentHour >= prefs.quietHoursStart || currentHour < prefs.quietHoursEnd
            } else {
                currentHour in prefs.quietHoursStart until prefs.quietHoursEnd
            }
            if (isQuiet) {
                return Result.success() // Quiet period, do nothing
            }
        }

        // 3. Check Active Hours
        if (prefs.activeHoursEnabled) {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isActive = if (prefs.activeHoursStart < prefs.activeHoursEnd) {
                currentHour in prefs.activeHoursStart until prefs.activeHoursEnd
            } else {
                currentHour >= prefs.activeHoursStart || currentHour < prefs.activeHoursEnd
            }
            if (!isActive) {
                return Result.success()
            }
        }

        // 4. Check Power constraints
        if (prefs.pauseOnPowerSaver && BatteryUtils.isPowerSaveMode(applicationContext)) {
            return Result.success()
        }

        if (prefs.requireCharging && !BatteryUtils.isCharging(applicationContext)) {
            return Result.success()
        }

        if (prefs.pauseOnLowBattery && BatteryUtils.getBatteryLevel(applicationContext) < 20 && !BatteryUtils.isCharging(applicationContext)) {
            return Result.success()
        }

        // 5. Execute wallpaper rotation
        val changeResult = repository.changeWallpaperNow()
        return when (changeResult) {
            is WallpaperApplyResult.Success -> Result.success()
            is WallpaperApplyResult.Failure -> {
                // If failed due to no wallpapers, try to seed or retry later
                Result.retry()
            }
        }
    }
}
