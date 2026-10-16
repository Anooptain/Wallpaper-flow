# Wallpaper Flow AI

**Wallpaper Flow AI** is a native Android application built in Kotlin with Jetpack Compose that automatically discovers, curates, generates, and applies high-resolution wallpapers to your Android device's Home and Lock screens according to your preferences and schedule.

---

## 🌟 Core Features

- **Automatic Background Wallpaper Rotation:** Uses Android's native `WorkManager` with configurable intervals (15 min, 30 min, 1h, 3h, 6h, 12h, 24h) and battery-optimized maintenance windows.
- **Three-Tier Priority Architecture:**
  1. *Local Rotation First:* Works 100% offline using bundled starter high-definition wallpapers and locally imported images.
  2. *Legitimate Online Discovery Second:* Curated high-resolution photography with full artist attribution and license metadata.
  3. *AI Studio Generation Third:* Custom prompt composer, style modifiers, color moods, and daily generation quota cost controls.
- **Independent Screen Targeting:** Apply wallpapers to Home Screen (`FLAG_SYSTEM`), Lock Screen (`FLAG_LOCK`), or Both (`FLAG_SYSTEM | FLAG_LOCK`).
- **Interactive Fullscreen Preview:** Test wallpapers before applying with a simulated lock screen clock, date, and navigation overlay.
- **Smart Battery & Network Policies:**
  - Wi-Fi only download constraint.
  - Require charging constraint.
  - Pause on low battery (<20%).
  - Pause during Android system Battery Saver mode.
  - Quiet Hours (Night mode) to prevent changes while sleeping.
- **Device Photo Import:** Pick custom photos from your device gallery using the zero-permission Android Photo Picker (`ActivityResultContracts.PickVisualMedia`).
- **Local Persistence & Cache Management:**
  - `Room Database`: Stores metadata, favorites, rotation history, and failure logs.
  - `Jetpack DataStore`: Persists preferences, intervals, and quota counters.
  - Cache size manager: Clear cached files anytime while safely preserving all favorited wallpapers.
- **Real-Time Diagnostics & Permissions Hub:** Complete live telemetry of WorkManager state, next run window, last success/error timestamps, network connectivity, and OEM battery restriction instructions.

---

## 🏗️ Technical Architecture

```
com.example/
├── MainActivity.kt                  # Navigation, bottom bar, edge-to-edge scaffolding
├── WallpaperFlowApplication.kt      # Application initialization & notification channels
├── data/
│   ├── model/                       # Domain models (Wallpaper, Category, Target, ScheduleInterval, Diagnostics)
│   ├── database/                    # Room (AppDatabase, WallpaperDao, WallpaperEntity, WallpaperHistoryEntity)
│   ├── preferences/                 # Jetpack DataStore (UserPreferencesRepository)
│   ├── network/providers/           # Wallpaper providers (CuratedOnlineProvider, LocalWallpaperManager, AiWallpaperEngine)
│   └── repository/                  # WallpaperRepository (unified data orchestrator)
├── scheduler/
│   ├── WallpaperWorker.kt           # CoroutineWorker handling background rotation
│   ├── WallpaperWorkManager.kt      # WorkManager scheduler & constraint builder
│   └── BootReceiver.kt              # Restores scheduling on device reboot
├── wallpaper/
│   └── WallpaperManagerHelper.kt    # Android WallpaperManager integration with safe downsampling
├── notifications/
│   └── NotificationHelper.kt        # Android 13+ Notification channels & change summaries
├── utils/                           # Memory, SHA-256 deduplication, Network, and Battery utilities
└── ui/
    ├── theme/                       # Modern M3 Dark/Light theme, obsidian-indigo palette
    ├── components/                  # WallpaperItemCard, WallpaperPreviewDialog
    └── screens/                     # HomeScreen, ExploreScreen, AiStudioScreen, FavoritesScreen, SettingsScreen, DiagnosticsScreen, PermissionsScreen, OnboardingScreen
```

---

## 🔒 Permissions & Privacy

Wallpaper Flow AI adheres to the Principle of Least Privilege:
- `android.permission.INTERNET`: For online wallpaper downloads and AI synthesis.
- `android.permission.ACCESS_NETWORK_STATE`: For detecting Wi-Fi vs mobile network.
- `android.permission.SET_WALLPAPER`: For Android `WallpaperManager` execution.
- `android.permission.RECEIVE_BOOT_COMPLETED`: Restores automation after phone reboot.
- `android.permission.POST_NOTIFICATIONS`: Contextually requested on Android 13+ for quiet change summaries.
- **Zero broad storage access:** Uses app-private sandbox storage and Android Photo Picker. No `READ_EXTERNAL_STORAGE` or `MANAGE_EXTERNAL_STORAGE`.
- **Zero location, camera, microphone, or contacts access.**

---

## 🔋 Battery Optimization & Android Limitations

1. **WorkManager Inexact Scheduling:** Android WorkManager has a strict minimum periodic interval of 15 minutes. Execution windows are inexact and battery-optimized by Android.
2. **Aggressive OEM Power Managers:** Devices by Samsung, Xiaomi, and OnePlus may restrict background activity. Navigate to **Settings > Permissions Hub** inside the app to access direct shortcuts to whitelist the app from battery restrictions.

---

## 📱 Phone-Only Build & Installation Instructions

If you are developing or building using only an Android phone:

1. **AI Studio In-Browser Build:**
   - Tap the top-right menu in AI Studio.
   - Select **Export / Build APK**.
2. **Download Debug APK:**
   - Download the generated `.apk` file directly to your Android device's Downloads folder.
3. **Install & Grant Permissions:**
   - Open your device's Files app, tap the APK, and choose **Install**.
   - If prompted, enable "Install unknown apps" for your browser/file manager.
4. **Setup in App:**
   - Complete the 4-step onboarding.
   - Choose your favorite categories and interval.
   - Tap **Change Wallpaper Now** to confirm your first wallpaper changes immediately!

---

## ✅ Verified Testing Checklist

- [x] Application compiles cleanly into debug APK.
- [x] Onboarding flow introduces features and timing constraints.
- [x] Room database stores wallpapers, favorites, and history.
- [x] DataStore preferences persist interval, screen target, categories, and quotas.
- [x] WallpaperManager successfully sets Home Screen, Lock Screen, and Both.
- [x] Local starter wallpapers render offline out of the box.
- [x] Photo Picker imports custom photos safely.
- [x] WorkManager schedules periodic background rotation with battery/network constraints.
- [x] Boot receiver registered for restart recovery.
- [x] AI Studio enforces daily generation quotas and handles missing API keys gracefully.
- [x] Cache clearing cleans temporary files while preserving favorited wallpapers.
