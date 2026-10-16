package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperSource

@Entity(tableName = "wallpapers")
data class WallpaperEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val authorUrl: String?,
    val imageUrl: String,
    val localFilePath: String?,
    val source: String,
    val category: String,
    val width: Int,
    val height: Int,
    val isFavorite: Boolean,
    val isExcluded: Boolean,
    val prompt: String?,
    val hashSha256: String?,
    val timestamp: Long,
    val lastAppliedTimestamp: Long?
) {
    fun toDomain(): Wallpaper {
        return Wallpaper(
            id = id,
            title = title,
            author = author,
            authorUrl = authorUrl,
            imageUrl = imageUrl,
            localFilePath = localFilePath,
            source = try { WallpaperSource.valueOf(source) } catch (e: Exception) { WallpaperSource.ONLINE },
            category = category,
            width = width,
            height = height,
            isFavorite = isFavorite,
            isExcluded = isExcluded,
            prompt = prompt,
            hashSha256 = hashSha256,
            timestamp = timestamp,
            lastAppliedTimestamp = lastAppliedTimestamp
        )
    }

    companion object {
        fun fromDomain(domain: Wallpaper): WallpaperEntity {
            return WallpaperEntity(
                id = domain.id,
                title = domain.title,
                author = domain.author,
                authorUrl = domain.authorUrl,
                imageUrl = domain.imageUrl,
                localFilePath = domain.localFilePath,
                source = domain.source.name,
                category = domain.category,
                width = domain.width,
                height = domain.height,
                isFavorite = domain.isFavorite,
                isExcluded = domain.isExcluded,
                prompt = domain.prompt,
                hashSha256 = domain.hashSha256,
                timestamp = domain.timestamp,
                lastAppliedTimestamp = domain.lastAppliedTimestamp
            )
        }
    }
}
