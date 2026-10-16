package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ScheduleInterval
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperCategory
import com.example.data.model.WallpaperSource
import com.example.data.model.WallpaperTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Wallpaper Flow", appName)
    }

    @Test
    fun `verify schedule intervals comply with minimum 15 minutes`() {
        ScheduleInterval.values().forEach { interval ->
            assertTrue(
                "WorkManager periodic work requires minimum 15 minutes interval: ${interval.name}",
                interval.minutes >= 15
            )
        }
    }

    @Test
    fun `verify wallpaper model creation and defaults`() {
        val wallpaper = Wallpaper(
            id = "test_1",
            title = "Misty Mountain",
            imageUrl = "https://example.com/mountain.jpg",
            source = WallpaperSource.LOCAL,
            category = "Mountains"
        )
        assertEquals("test_1", wallpaper.id)
        assertEquals("Mountains", wallpaper.category)
        assertFalse(wallpaper.isFavorite)
        assertFalse(wallpaper.isExcluded)
    }

    @Test
    fun `verify wallpaper targets title`() {
        assertEquals("Home Screen", WallpaperTarget.HOME.title)
        assertEquals("Lock Screen", WallpaperTarget.LOCK.title)
        assertEquals("Home & Lock Screens", WallpaperTarget.BOTH.title)
    }
}
