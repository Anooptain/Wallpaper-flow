package com.example.scheduler

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.model.ScheduleInterval
import java.util.concurrent.TimeUnit

class WallpaperWorkManager(private val context: Context) {

    companion object {
        const val PERIODIC_WORK_NAME = "wallpaper_periodic_flow_work"
        const val ONE_TIME_WORK_NAME = "wallpaper_one_time_change_work"
    }

    private fun getWorkManager(): WorkManager? {
        return try {
            WorkManager.getInstance(context)
        } catch (e: Exception) {
            null
        }
    }

    fun schedulePeriodicAutomation(
        interval: ScheduleInterval,
        wifiOnly: Boolean,
        requireCharging: Boolean,
        batteryNotLow: Boolean
    ) {
        val workManager = getWorkManager() ?: return

        // Build constraints
        val constraints = Constraints.Builder().apply {
            if (wifiOnly) {
                setRequiredNetworkType(NetworkType.UNMETERED)
            } else {
                setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            }
            if (requireCharging) {
                setRequiresCharging(true)
            }
            if (batteryNotLow) {
                setRequiresBatteryNotLow(true)
            }
        }.build()

        // WorkManager minimum periodic interval is 15 minutes
        val minutes = interval.minutes.coerceAtLeast(15)

        val periodicWorkRequest = PeriodicWorkRequestBuilder<WallpaperWorker>(
            minutes,
            TimeUnit.MINUTES,
            5, // Flex interval: 5 minutes flex window
            TimeUnit.MINUTES
        ).setConstraints(constraints)
         .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
    }

    fun cancelPeriodicAutomation() {
        getWorkManager()?.cancelUniqueWork(PERIODIC_WORK_NAME)
    }

    fun triggerOneTimeChange() {
        val workManager = getWorkManager() ?: return
        val oneTimeRequest = OneTimeWorkRequestBuilder<WallpaperWorker>()
            .build()
        workManager.enqueueUniqueWork(
            ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )
    }
}
