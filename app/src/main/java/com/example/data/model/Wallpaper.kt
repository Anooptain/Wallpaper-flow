package com.example.data.model

enum class WallpaperSource {
    LOCAL,
    ONLINE,
    AI,
    CACHED
}

enum class WallpaperTarget(val title: String) {
    HOME("Home Screen"),
    LOCK("Lock Screen"),
    BOTH("Home & Lock Screens")
}

data class Wallpaper(
    val id: String,
    val title: String,
    val author: String = "Unknown",
    val authorUrl: String? = null,
    val imageUrl: String,
    val localFilePath: String? = null,
    val source: WallpaperSource = WallpaperSource.ONLINE,
    val category: String = "Nature",
    val width: Int = 1080,
    val height: Int = 2400,
    val isFavorite: Boolean = false,
    val isExcluded: Boolean = false,
    val prompt: String? = null,
    val hashSha256: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val lastAppliedTimestamp: Long? = null
)
