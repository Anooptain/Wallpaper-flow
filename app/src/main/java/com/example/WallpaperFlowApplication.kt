package com.example

import android.app.Application
import com.example.data.repository.WallpaperRepository
import com.example.notifications.NotificationHelper
import com.example.scheduler.WallpaperWorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WallpaperFlowApplication : Application() {

    lateinit var repository: WallpaperRepository
        private set

    lateinit var workManagerHelper: WallpaperWorkManager
        private set

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        repository = WallpaperRepository(this)
        workManagerHelper = WallpaperWorkManager(this)

        try {
            // Initialize Notification Channels
            NotificationHelper.createNotificationChannels(this)
        } catch (e: Exception) {
            // Safe fallback
        }

        // Asynchronously seed starter local wallpapers & ensure scheduler is in sync
        applicationScope.launch(Dispatchers.IO) {
            try {
                repository.initialize()
                val prefs = repository.userPreferences.first()
                if (prefs.isAutomationEnabled) {
                    workManagerHelper.schedulePeriodicAutomation(
                        interval = prefs.interval,
                        wifiOnly = prefs.wifiOnly,
                        requireCharging = prefs.requireCharging,
                        batteryNotLow = prefs.pauseOnLowBattery
                    )
                }
            } catch (e: Exception) {
                // Ignore background initialization errors in restricted test environments
            }
        }
    }
}
