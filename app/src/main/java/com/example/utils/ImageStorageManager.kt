package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest

class ImageStorageManager(private val context: Context) {

    private val wallpaperDir: File
        get() {
            val dir = File(context.filesDir, "wallpapers")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    suspend fun saveImageToFile(fileName: String, inputStream: InputStream): File = withContext(Dispatchers.IO) {
        val targetFile = File(wallpaperDir, fileName)
        FileOutputStream(targetFile).use { output ->
            inputStream.copyTo(output)
        }
        targetFile
    }

    suspend fun saveBitmapToFile(fileName: String, bitmap: Bitmap): File = withContext(Dispatchers.IO) {
        val targetFile = File(wallpaperDir, fileName)
        FileOutputStream(targetFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)
        }
        targetFile
    }

    suspend fun calculateSha256(file: File): String = withContext(Dispatchers.IO) {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            val bytes = digest.digest()
            val sb = StringBuilder()
            for (b in bytes) {
                sb.append(String.format("%02x", b))
            }
            sb.toString()
        } catch (e: Exception) {
            file.name.hashCode().toString()
        }
    }

    suspend fun decodeSampledBitmapFromFile(
        file: File,
        reqWidth: Int = 1080,
        reqHeight: Int = 2400
    ): Bitmap? = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext null

        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: OutOfMemoryError) {
            // Fallback with larger inSampleSize
            try {
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 4
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                BitmapFactory.decodeFile(file.absolutePath, options)
            } catch (t: Throwable) {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    suspend fun getStorageUsedMb(): Double = withContext(Dispatchers.IO) {
        try {
            var totalBytes: Long = 0
            val files = wallpaperDir.listFiles()
            if (files != null) {
                for (f in files) {
                    if (f.isFile) totalBytes += f.length()
                }
            }
            (totalBytes / (1024.0 * 1024.0))
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun cleanCachePreservingFavorites(database: AppDatabase): Int = withContext(Dispatchers.IO) {
        try {
            val dao = database.wallpaperDao()
            val nonFavorites = dao.getNonFavoriteWallpapersOldestFirst()
            var deletedCount = 0

            for (entity in nonFavorites) {
                if (entity.localFilePath != null) {
                    val file = File(entity.localFilePath)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                dao.deleteWallpaper(entity.id)
                deletedCount++
            }
            deletedCount
        } catch (e: Exception) {
            0
        }
    }
}
