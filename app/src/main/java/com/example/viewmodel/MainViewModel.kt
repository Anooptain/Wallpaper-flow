package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.WallpaperFlowApplication
import com.example.data.database.WallpaperHistoryEntity
import com.example.data.model.DiagnosticInfo
import com.example.data.model.ScheduleInterval
import com.example.data.model.UserPreferences
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperTarget
import com.example.data.network.providers.AiGenerationResult
import com.example.wallpaper.WallpaperApplyResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String) {
    HOME("Home"),
    EXPLORE("Explore"),
    AI_STUDIO("AI Studio"),
    FAVORITES("Favorites"),
    SETTINGS("Settings")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as WallpaperFlowApplication
    private val repository = app.repository
    private val workManagerHelper = app.workManagerHelper

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    val allWallpapers: StateFlow<List<Wallpaper>> = repository.allWallpapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteWallpapers: StateFlow<List<Wallpaper>> = repository.favoriteWallpapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentHistory: StateFlow<List<WallpaperHistoryEntity>> = repository.recentHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _onlineWallpapers = MutableStateFlow<List<Wallpaper>>(emptyList())
    val onlineWallpapers: StateFlow<List<Wallpaper>> = _onlineWallpapers.asStateFlow()

    private val _isLoadingOnline = MutableStateFlow(false)
    val isLoadingOnline: StateFlow<Boolean> = _isLoadingOnline.asStateFlow()

    private val _selectedExploreCategory = MutableStateFlow("All")
    val selectedExploreCategory: StateFlow<String> = _selectedExploreCategory.asStateFlow()

    private val _exploreSearchQuery = MutableStateFlow("")
    val exploreSearchQuery: StateFlow<String> = _exploreSearchQuery.asStateFlow()

    private val _diagnostics = MutableStateFlow<DiagnosticInfo?>(null)
    val diagnostics: StateFlow<DiagnosticInfo?> = _diagnostics.asStateFlow()

    private val _previewWallpaper = MutableStateFlow<Wallpaper?>(null)
    val previewWallpaper: StateFlow<Wallpaper?> = _previewWallpaper.asStateFlow()

    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi: StateFlow<Boolean> = _isGeneratingAi.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _isApplying = MutableStateFlow(false)
    val isApplying: StateFlow<Boolean> = _isApplying.asStateFlow()

    init {
        loadOnlineWallpapers()
        refreshDiagnostics()
    }

    fun loadOnlineWallpapers(category: String = _selectedExploreCategory.value, query: String? = _exploreSearchQuery.value) {
        viewModelScope.launch {
            _isLoadingOnline.value = true
            val result = repository.fetchOnlineWallpapers(category, query)
            result.onSuccess { list ->
                _onlineWallpapers.value = list
            }.onFailure { err ->
                _snackbarMessage.value = "Unable to fetch online wallpapers: ${err.message}"
            }
            _isLoadingOnline.value = false
        }
    }

    fun setExploreCategory(category: String) {
        _selectedExploreCategory.value = category
        loadOnlineWallpapers(category, _exploreSearchQuery.value)
    }

    fun setExploreSearchQuery(query: String) {
        _exploreSearchQuery.value = query
        loadOnlineWallpapers(_selectedExploreCategory.value, query)
    }

    fun changeWallpaperNow() {
        viewModelScope.launch {
            _isApplying.value = true
            val result = repository.changeWallpaperNow()
            when (result) {
                is WallpaperApplyResult.Success -> {
                    _snackbarMessage.value = "Wallpaper changed to \"${result.wallpaper.title}\" successfully!"
                }
                is WallpaperApplyResult.Failure -> {
                    _snackbarMessage.value = "Failed to change wallpaper: ${result.errorMessage}"
                }
            }
            _isApplying.value = false
            refreshDiagnostics()
        }
    }

    fun toggleAutomation(enabled: Boolean) {
        viewModelScope.launch {
            repository.preferencesRepository.setAutomationEnabled(enabled)
            val prefs = userPreferences.value
            if (enabled) {
                workManagerHelper.schedulePeriodicAutomation(
                    interval = prefs.interval,
                    wifiOnly = prefs.wifiOnly,
                    requireCharging = prefs.requireCharging,
                    batteryNotLow = prefs.pauseOnLowBattery
                )
                _snackbarMessage.value = "Automatic wallpaper rotation enabled."
            } else {
                workManagerHelper.cancelPeriodicAutomation()
                _snackbarMessage.value = "Automatic wallpaper rotation paused."
            }
            refreshDiagnostics()
        }
    }

    fun applyWallpaper(wallpaper: Wallpaper, target: WallpaperTarget) {
        viewModelScope.launch {
            _isApplying.value = true
            // If it's an online wallpaper without local file, download first
            val wallpaperToApply = if (wallpaper.localFilePath == null) {
                val dlResult = repository.downloadAndCacheWallpaper(wallpaper)
                dlResult.getOrNull() ?: wallpaper
            } else {
                wallpaper
            }

            val result = repository.applyWallpaper(wallpaperToApply, target)
            when (result) {
                is WallpaperApplyResult.Success -> {
                    _snackbarMessage.value = "Applied \"${wallpaper.title}\" to ${target.title}!"
                    _previewWallpaper.value = null
                }
                is WallpaperApplyResult.Failure -> {
                    _snackbarMessage.value = "Could not apply wallpaper: ${result.errorMessage}"
                }
            }
            _isApplying.value = false
            refreshDiagnostics()
        }
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch {
            repository.toggleFavorite(wallpaper.id, wallpaper.isFavorite)
        }
    }

    fun deleteWallpaper(wallpaperId: String) {
        viewModelScope.launch {
            repository.deleteWallpaper(wallpaperId)
            _snackbarMessage.value = "Wallpaper removed from local storage."
            refreshDiagnostics()
        }
    }

    fun generateAiWallpaper(prompt: String, style: String, primaryHex: String, secondaryHex: String) {
        viewModelScope.launch {
            _isGeneratingAi.value = true
            _aiError.value = null
            val result = repository.generateAiWallpaper(prompt, style, primaryHex, secondaryHex)
            when (result) {
                is AiGenerationResult.Success -> {
                    _previewWallpaper.value = result.wallpaper
                    _snackbarMessage.value = "AI Wallpaper generated successfully!"
                }
                is AiGenerationResult.QuotaExceeded -> {
                    _aiError.value = "Daily quota reached (${result.limit}/${result.limit}). You can still use downloaded and locally stored wallpapers."
                }
                is AiGenerationResult.Error -> {
                    _aiError.value = result.message
                }
            }
            _isGeneratingAi.value = false
            refreshDiagnostics()
        }
    }

    fun importLocalPhoto(uri: Uri, title: String, category: String) {
        viewModelScope.launch {
            val result = repository.importPhoto(uri, title, category)
            result.onSuccess {
                _snackbarMessage.value = "Imported photo \"${it.title}\" successfully!"
                refreshDiagnostics()
            }.onFailure {
                _snackbarMessage.value = "Failed to import photo: ${it.message}"
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            val count = repository.clearCache()
            _snackbarMessage.value = "Cleaned $count cached wallpapers. Favorites were preserved."
            refreshDiagnostics()
        }
    }

    fun refreshDiagnostics() {
        viewModelScope.launch {
            _diagnostics.value = repository.getDiagnostics()
        }
    }

    fun updateScheduleInterval(interval: ScheduleInterval) {
        viewModelScope.launch {
            repository.preferencesRepository.setInterval(interval)
            val prefs = userPreferences.value
            if (prefs.isAutomationEnabled) {
                workManagerHelper.schedulePeriodicAutomation(
                    interval = interval,
                    wifiOnly = prefs.wifiOnly,
                    requireCharging = prefs.requireCharging,
                    batteryNotLow = prefs.pauseOnLowBattery
                )
            }
            refreshDiagnostics()
        }
    }

    fun updateTarget(target: WallpaperTarget) {
        viewModelScope.launch {
            repository.preferencesRepository.setTarget(target)
            refreshDiagnostics()
        }
    }

    fun updateSelectedCategories(categories: Set<String>) {
        viewModelScope.launch {
            repository.preferencesRepository.setSelectedCategories(categories)
        }
    }

    fun updateActiveHours(enabled: Boolean, start: Int, end: Int) {
        viewModelScope.launch {
            repository.preferencesRepository.setActiveHours(enabled, start, end)
        }
    }

    fun updateQuietHours(enabled: Boolean, start: Int, end: Int) {
        viewModelScope.launch {
            repository.preferencesRepository.setQuietHours(enabled, start, end)
        }
    }

    fun updateNetworkConstraints(wifiOnly: Boolean, chargeOnly: Boolean, lowBatt: Boolean, powerSaver: Boolean) {
        viewModelScope.launch {
            repository.preferencesRepository.setWifiOnly(wifiOnly)
            repository.preferencesRepository.setRequireCharging(chargeOnly)
            repository.preferencesRepository.setPauseOnLowBattery(lowBatt)
            repository.preferencesRepository.setPauseOnPowerSaver(powerSaver)

            val prefs = userPreferences.value
            if (prefs.isAutomationEnabled) {
                workManagerHelper.schedulePeriodicAutomation(
                    interval = prefs.interval,
                    wifiOnly = wifiOnly,
                    requireCharging = chargeOnly,
                    batteryNotLow = lowBatt
                )
            }
            refreshDiagnostics()
        }
    }

    fun updateNotifyOnChange(notify: Boolean) {
        viewModelScope.launch {
            repository.preferencesRepository.setNotifyOnChange(notify)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.preferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun setPreviewWallpaper(wallpaper: Wallpaper?) {
        _previewWallpaper.value = wallpaper
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
