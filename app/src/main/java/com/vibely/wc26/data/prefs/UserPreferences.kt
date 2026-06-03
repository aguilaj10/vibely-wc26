package com.vibely.wc26.data.prefs

enum class ThemeMode { System, Light, Dark }
enum class PlayerSort { Slot, Alphabetical }

data class UserPreferences(
    val theme: ThemeMode = ThemeMode.System,
    val playerSort: PlayerSort = PlayerSort.Slot,
    val rapidScan: Boolean = false,
)
