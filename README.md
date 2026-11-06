# Wallpaper Flow AI

**Your wallpaper. Your style. Automatically.**

Wallpaper Flow AI is a native Android application built with Kotlin and Jetpack Compose. It automatically discovers, organizes, generates, and changes wallpapers on your Android Home Screen and Lock Screen based on your preferences.

Enjoy a fresh wallpaper without manually changing it every day.

## ✨ Features

- **Automatic Wallpaper Rotation** – Automatically changes wallpapers on a configurable schedule.
- **Offline Support** – Use bundled wallpapers and your own imported photos without an internet connection.
- **Online Wallpaper Discovery** – Discover curated high-resolution photography with artist attribution and licensing information.
- **AI Wallpaper Studio** – Create personalized wallpapers using custom prompts, styles, and color preferences.
- **Home & Lock Screen Control** – Apply wallpapers to your Home Screen, Lock Screen, or both.
- **Fullscreen Preview** – Preview wallpapers before applying them.
- **Custom Photo Import** – Select your favorite images directly from your device gallery.
- **Favorites** – Save wallpapers you love.
- **Smart Battery Management** – Configure Wi-Fi-only downloads, charging requirements, low-battery protection, and quiet hours.
- **Automatic Restart Recovery** – Restore scheduled wallpaper rotation after a device reboot.
- **Diagnostics Dashboard** – Monitor background tasks, scheduling, network status, and errors.

## 🎨 Wallpaper Sources

Wallpaper Flow AI uses a three-level priority system:

1. **Local Wallpapers:** Bundled wallpapers and images imported from your device.
2. **Online Discovery:** Curated photography with attribution and license metadata.
3. **AI Studio:** Generate custom wallpapers using your own creative prompts.

## ⏰ Automatic Scheduling

Choose how frequently your wallpaper changes:

- Every 15 minutes
- Every 30 minutes
- Every 1 hour
- Every 3 hours
- Every 6 hours
- Every 12 hours
- Every 24 hours

Scheduling is managed through Android WorkManager.

**Note:** Android controls background execution timing. Scheduled changes are not guaranteed to happen at the exact selected minute.

## 🔋 Battery Optimization

The application includes several options to reduce unnecessary battery and network usage:

- Wi-Fi-only downloads
- Require charging
- Pause below 20% battery
- Respect Android Battery Saver
- Quiet hours to prevent nighttime wallpaper changes

Some Android manufacturers may restrict background applications. You may need to allow unrestricted background activity in your device settings.

## 🔐 Privacy & Permissions

Wallpaper Flow AI follows the principle of minimum permissions.

| Permission | Purpose |
|---|---|
| Internet | Online wallpapers and AI services |
| Network State | Detect network connectivity |
| Set Wallpaper | Apply wallpapers |
| Receive Boot Completed | Restore scheduled tasks |
| Post Notifications | Wallpaper change notifications |

The application uses Android Photo Picker for importing personal images without requesting broad storage access.

It does not require location, camera, microphone, or contacts permissions.

## 🛠️ Technology Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** Repository-based architecture
- **Background Tasks:** Android WorkManager
- **Local Database:** Room
- **Preferences:** Jetpack DataStore
- **Wallpaper API:** Android WallpaperManager
- **Concurrency:** Kotlin Coroutines

## 📱 Installation

1. Download the latest APK from the project's GitHub Releases.
2. Open the downloaded APK on your Android device.
3. Allow installation from unknown sources if Android asks.
4. Install and launch Wallpaper Flow AI.
5. Complete onboarding and select your wallpaper preferences.
6. Configure automatic rotation.

## 🚀 Build from Source

This project is developed as a native Android application.

To build it:

1. Clone this repository.
2. Open the project in Android Studio.
3. Allow Gradle to synchronize dependencies.
4. Configure any required API credentials for AI functionality.
5. Build and run the application.

```bash
git clone https://github.com/Anooptain/Wallpaper-flow.git
