package com.example.data.model

enum class WallpaperCategory(val displayName: String, val iconName: String) {
    NATURE("Nature", "Forest"),
    MOUNTAINS("Mountains", "Landscape"),
    FORESTS("Forests", "Park"),
    MONSOON("Monsoon", "WaterDrop"),
    RAINY_LANDSCAPES("Rainy Landscapes", "CloudRain"),
    WATERFALLS("Waterfalls", "Waves"),
    SPACE("Space", "RocketLaunch"),
    GALAXIES("Galaxies", "AutoAwesome"),
    PLANETS("Planets", "Public"),
    AMOLED_BLACK("AMOLED Black", "DarkMode"),
    DARK_AESTHETIC("Dark Aesthetic", "Bedtime"),
    MINIMALIST("Minimalist", "CropPortrait"),
    ABSTRACT("Abstract", "Palette"),
    SUPERCARS("Supercars", "DirectionsCar"),
    MOTORCYCLES("Motorcycles", "TwoWheeler"),
    GAMING("Gaming", "SportsEsports"),
    ANIME("Anime", "ColorLens"),
    ARCHITECTURE("Architecture", "Apartment"),
    FUTURISTIC_CITIES("Futuristic Cities", "LocationCity"),
    PHOTOGRAPHY("Photography", "CameraAlt"),
    CINEMATIC("Cinematic", "Movie"),
    AI_GENERATED("AI Generated", "SmartToy")
}

enum class ScheduleInterval(val displayName: String, val minutes: Long) {
    MIN_15("Every 15 minutes", 15),
    MIN_30("Every 30 minutes", 30),
    HOUR_1("Every hour", 60),
    HOUR_3("Every 3 hours", 180),
    HOUR_6("Every 6 hours", 360),
    HOUR_12("Every 12 hours", 720),
    DAY_1("Once per day", 1440)
}
