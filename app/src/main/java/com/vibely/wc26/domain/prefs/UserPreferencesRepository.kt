package com.vibely.wc26.domain.prefs

import kotlinx.coroutines.flow.Flow

/**
 * Domain port for user preferences. Implementation lives in the data layer
 * (DataStore-backed); domain and presentation depend only on this interface.
 */
interface UserPreferencesRepository {

    val preferences: Flow<UserPreferences>

    suspend fun setTheme(mode: ThemeMode)
    suspend fun setPlayerSort(sort: PlayerSort)
    suspend fun setRapidScan(enabled: Boolean)
    suspend fun setLastAddedSticker(stickerId: String)
    suspend fun markTeamCelebrated(teamCode: String)
}
