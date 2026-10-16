package com.example.data.network.providers

import com.example.data.model.Wallpaper

interface WallpaperProvider {
    val providerName: String
    suspend fun fetchWallpapers(category: String, page: Int, query: String? = null): Result<List<Wallpaper>>
}
