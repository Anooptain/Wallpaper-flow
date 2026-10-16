package com.example.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val prefsRepo = UserPreferencesRepository(context)
                    val prefs = prefsRepo.userPreferencesFlow.first()
                    if (prefs.isAutomationEnabled) {
                        val scheduler = WallpaperWorkManager(context)
                        scheduler.schedulePeriodicAutomation(
                            interval = prefs.interval,
                            wifiOnly = prefs.wifiOnly,
                            requireCharging = prefs.requireCharging,
                            batteryNotLow = prefs.pauseOnLowBattery
                        )
                    }
                } catch (e: Exception) {
                    // Ignore restore failures on boot
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
