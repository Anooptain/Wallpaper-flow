package com.example.data.network.providers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.net.Uri
import com.example.data.database.AppDatabase
import com.example.data.database.WallpaperEntity
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperSource
import com.example.utils.ImageStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class LocalWallpaperManager(private val context: Context) {

    private val imageStorageManager = ImageStorageManager(context)
    private val database = AppDatabase.getInstance(context)

    suspend fun seedStarterWallpapersIfEmpty() = withContext(Dispatchers.IO) {
        val dao = database.wallpaperDao()
        if (dao.getWallpaperCount() > 0) return@withContext

        // Create 4 initial distinct, high-resolution artistic wallpapers
        val starters = listOf(
            StarterTemplate("starter_aurora", "Nordic Aurora Sky", "Local Art Studio", "AMOLED Black", "#050510", "#1E1B4B", "#38BDF8"),
            StarterTemplate("starter_sunset", "Canyon Crimson Sunset", "Local Art Studio", "Mountains", "#1E0515", "#831843", "#F59E0B"),
            StarterTemplate("starter_mist", "Misty Bamboo Forest", "Local Art Studio", "Forests", "#022C22", "#065F46", "#34D399"),
            StarterTemplate("starter_cosmic", "Deep Void Nebula", "Local Art Studio", "Space", "#020617", "#0F172A", "#818CF8")
        )

        for (item in starters) {
            val bitmap = generateArtisticGradientBitmap(item.topColor, item.midColor, item.bottomColor)
            val fileName = "${item.id}.jpg"
            val file = imageStorageManager.saveBitmapToFile(fileName, bitmap)
            val hash = imageStorageManager.calculateSha256(file)

            val wallpaper = Wallpaper(
                id = item.id,
                title = item.title,
                author = item.author,
                imageUrl = file.toURI().toString(),
                localFilePath = file.absolutePath,
                source = WallpaperSource.LOCAL,
                category = item.category,
                width = 1080,
                height = 2400,
                hashSha256 = hash,
                timestamp = System.currentTimeMillis()
            )
            dao.insertWallpaper(WallpaperEntity.fromDomain(wallpaper))
        }
    }

    suspend fun importUserSelectedPhoto(uri: Uri, title: String, category: String): Result<Wallpaper> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open selected image"))

            val id = "local_user_${UUID.randomUUID()}"
            val fileName = "$id.jpg"
            val file = imageStorageManager.saveImageToFile(fileName, inputStream)
            val hash = imageStorageManager.calculateSha256(file)

            val wallpaper = Wallpaper(
                id = id,
                title = title.ifBlank { "Imported Photo" },
                author = "My Device",
                imageUrl = file.toURI().toString(),
                localFilePath = file.absolutePath,
                source = WallpaperSource.LOCAL,
                category = category,
                width = 1080,
                height = 2400,
                hashSha256 = hash,
                timestamp = System.currentTimeMillis()
            )

            database.wallpaperDao().insertWallpaper(WallpaperEntity.fromDomain(wallpaper))
            Result.success(wallpaper)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateArtisticGradientBitmap(topHex: String, midHex: String, bottomHex: String): Bitmap {
        val width = 1080
        val height = 2400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background Gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(Color.parseColor(topHex), Color.parseColor(midHex), Color.parseColor(bottomHex)),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Artistic geometric silhouette mountains
        val mountainPaint = Paint().apply {
            color = Color.parseColor("#090D16")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val path = Path().apply {
            moveTo(0f, height * 0.72f)
            lineTo(width * 0.35f, height * 0.58f)
            lineTo(width * 0.7f, height * 0.68f)
            lineTo(width.toFloat(), height * 0.54f)
            lineTo(width.toFloat(), height.toFloat())
            lineTo(0f, height.toFloat())
            close()
        }
        canvas.drawPath(path, mountainPaint)

        // Celestial moon / orb
        val orbPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            alpha = 180
            isAntiAlias = true
        }
        canvas.drawCircle(width * 0.75f, height * 0.32f, 90f, orbPaint)

        return bitmap
    }

    private data class StarterTemplate(
        val id: String,
        val title: String,
        val author: String,
        val category: String,
        val topColor: String,
        val midColor: String,
        val bottomColor: String
    )
}
