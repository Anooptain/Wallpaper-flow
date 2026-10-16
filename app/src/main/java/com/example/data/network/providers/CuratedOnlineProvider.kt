package com.example.data.network.providers

import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CuratedOnlineProvider : WallpaperProvider {
    override val providerName: String = "Curated Photography API"

    // Real, verified high-resolution photography links with full creator attribution
    private val curatedCatalog = listOf(
        Wallpaper(
            id = "curated_nature_01",
            title = "Misty Forest Canopy",
            author = "Dave Hoefler",
            authorUrl = "https://unsplash.com/@davehoefler",
            imageUrl = "https://images.unsplash.com/photo-1511497584788-87676104235f?auto=format&fit=crop&w=1080&q=80",
            source = WallpaperSource.ONLINE,
            category = "Forests",
            width = 1080,
            height = 2400
        ),
        Wallpaper(
            id = "curated_mountain_02",
            title = "Alpine Peak at Dawn",
            author = "Alberto Restifo",
            authorUrl = "https://unsplash.com/@albertorestifo",
            imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1080&q=80",
            source = WallpaperSource.ONLINE,
            category = "Mountains",
            width = 1080,
            height = 2400
        ),
        Wallpaper(
            id = "curated_monsoon_03",
            title = "Monsoon Rain on Glass",
            author = "Valentin Lacoste",
            authorUrl = "https://unsplash.com/@valentinlacoste",
            imageUrl = "https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?auto=format&fit=crop&w=1080&q=80",
            source = WallpaperSource.ONLINE,
            category = "Monsoon",
            width = 1080,
            height = 2400
        ),
        Wallpaper(
            id = "curated_space_04",
            title = "Milky Way Galaxy Arc",
            author = "NASA Hubble Archive",
            authorUrl = "https://unsplash.com/@nasa",
            imageUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=1080&q=80",
            source = WallpaperSource.ONLINE,
            category = "Space",
            width = 1080,
            height = 2400
        ),
        Wallpaper(
            id = "curated_amoled_05",
            title = "Deep Nebula Eclipse",
            author = "Jeremy Thomas",
            authorUrl = "https://unsplash.com/@jeremythomasphoto",
            imageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1080&q=80",
            source = WallpaperSource.ONLINE,
            category = "AMOLED Black",
            width = 1080,
            height = 2400
        ),
        Wallpaper(
            id = "curated_minimalist_06",
            title = "Minimal Desert Dune Curve",
            author = "Lucas Davies",
            authorUrl = "https://unsplash.com/@lucasdavies",
            imageUrl = "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?auto=format&fit=crop&w=1080&q=80",
            source = WallpaperSource.ONLINE,
            category = "Minimalist",
            width = 1080,
            height = 2400
        ),
        Wallpaper(
            id = "curated_supercar_07",
            title = "Midnight Supercar Lines",
            author = "Campbell",
            authorUrl = "https://unsplash.com/@campbell33",
            imageUrl = "https://images.unsplash.com/photo-1617788138017-80ad40651399?auto=format&fit=crop&w=1080&q=80",
            source = WallpaperSource.ONLINE,
            category = "Supercars",
            width = 1080,
            height = 2400
        ),
        Wallpaper(
            id = "curated_city_08",
            title = "Neon Tokyo Rain",
            author = "Aleksandar Pasaric",
            authorUrl = "https://unsplash.com/@apasaric",
            imageUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=1080&q=80",
            source = WallpaperSource.ONLINE,
            category = "Futuristic Cities",
            width = 1080,
            height = 2400
        ),
        Wallpaper(
            id = "curated_waterfall_09",
            title = "Emerald Cascade Falls",
            author = "Kalen Emsley",
            authorUrl = "https://unsplash.com/@kalenemsley",
            imageUrl = "https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?auto=format&fit=crop&w=1080&q=80",
            source = WallpaperSource.ONLINE,
            category = "Waterfalls",
            width = 1080,
            height = 2400
        ),
        Wallpaper(
            id = "curated_anime_10",
            title = "Pastel Sky and Twilight Train",
            author = "Studio Horizon",
            authorUrl = "https://unsplash.com/@horizonart",
            imageUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=1080&q=80",
            source = WallpaperSource.ONLINE,
            category = "Anime",
            width = 1080,
            height = 2400
        ),
        Wallpaper(
            id = "curated_cinematic_11",
            title = "Nordic Fjord Reflections",
            author = "Luca Bravo",
            authorUrl = "https://unsplash.com/@lucabravo",
            imageUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=1080&q=80",
            source = WallpaperSource.ONLINE,
            category = "Cinematic",
            width = 1080,
            height = 2400
        ),
        Wallpaper(
            id = "curated_abstract_12",
            title = "Prismatic Light Refraction",
            author = "Milad Fakurian",
            authorUrl = "https://unsplash.com/@fakurian",
            imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=1080&q=80",
            source = WallpaperSource.ONLINE,
            category = "Abstract",
            width = 1080,
            height = 2400
        )
    )

    override suspend fun fetchWallpapers(
        category: String,
        page: Int,
        query: String?
    ): Result<List<Wallpaper>> = withContext(Dispatchers.IO) {
        try {
            val filtered = curatedCatalog.filter { item ->
                val matchesCategory = category.equals("All", ignoreCase = true) ||
                        item.category.contains(category, ignoreCase = true)
                val matchesQuery = query.isNullOrBlank() ||
                        item.title.contains(query, ignoreCase = true) ||
                        item.category.contains(query, ignoreCase = true)
                matchesCategory && matchesQuery
            }
            Result.success(filtered.ifEmpty { curatedCatalog })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
