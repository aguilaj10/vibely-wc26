package com.vibely.wc26.domain.prefs

/**
 * Snapshot of user preferences. Stored in DataStore by the data layer; consumed
 * from this domain shape so use cases and ViewModels don't depend on data details.
 */
data class UserPreferences(
    val theme: ThemeMode = ThemeMode.System,
    val playerSort: PlayerSort = PlayerSort.Slot,
    val rapidScan: Boolean = false,
    /** Sticker id of the most recently incremented sticker. Surfaced on Home. */
    val lastAddedStickerId: String? = null,
    /** Team codes that have already received the one-shot 100% celebration. */
    val celebratedTeams: Set<String> = emptySet(),
)
