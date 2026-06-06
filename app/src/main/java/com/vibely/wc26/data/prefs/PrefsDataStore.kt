package com.vibely.wc26.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vibely.wc26.domain.prefs.PlayerSort
import com.vibely.wc26.domain.prefs.ThemeMode
import com.vibely.wc26.domain.prefs.UserPreferences
import com.vibely.wc26.domain.prefs.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.preferencesStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_prefs",
)

private object Keys {
    val THEME = stringPreferencesKey("theme")
    val PLAYER_SORT = stringPreferencesKey("player_sort")
    val RAPID_SCAN = booleanPreferencesKey("rapid_scan")
    val LAST_ADDED_STICKER_ID = stringPreferencesKey("last_added_sticker_id")
    val CELEBRATED_TEAMS = stringSetPreferencesKey("celebrated_teams")
}

@Singleton
internal class PrefsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserPreferencesRepository {

    private val store: DataStore<Preferences> = context.preferencesStore

    override val preferences: Flow<UserPreferences> = store.data.map { p ->
        UserPreferences(
            theme = p[Keys.THEME]?.let(::themeFromString) ?: ThemeMode.System,
            playerSort = p[Keys.PLAYER_SORT]?.let(::sortFromString) ?: PlayerSort.Slot,
            rapidScan = p[Keys.RAPID_SCAN] ?: false,
            lastAddedStickerId = p[Keys.LAST_ADDED_STICKER_ID],
            celebratedTeams = p[Keys.CELEBRATED_TEAMS].orEmpty(),
        )
    }

    override suspend fun setTheme(mode: ThemeMode) {
        store.edit { it[Keys.THEME] = mode.name }
    }

    override suspend fun setPlayerSort(sort: PlayerSort) {
        store.edit { it[Keys.PLAYER_SORT] = sort.name }
    }

    override suspend fun setRapidScan(enabled: Boolean) {
        store.edit { it[Keys.RAPID_SCAN] = enabled }
    }

    override suspend fun setLastAddedSticker(stickerId: String) {
        store.edit { it[Keys.LAST_ADDED_STICKER_ID] = stickerId }
    }

    override suspend fun markTeamCelebrated(teamCode: String) {
        store.edit { prefs ->
            val current = prefs[Keys.CELEBRATED_TEAMS].orEmpty()
            if (teamCode !in current) {
                prefs[Keys.CELEBRATED_TEAMS] = current + teamCode
            }
        }
    }

    private fun themeFromString(raw: String): ThemeMode =
        ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.System

    private fun sortFromString(raw: String): PlayerSort =
        PlayerSort.entries.firstOrNull { it.name == raw } ?: PlayerSort.Slot
}
