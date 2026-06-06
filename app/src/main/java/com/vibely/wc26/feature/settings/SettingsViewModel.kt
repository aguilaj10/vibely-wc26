package com.vibely.wc26.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.domain.ownership.OwnershipRepository
import com.vibely.wc26.domain.prefs.PlayerSort
import com.vibely.wc26.domain.prefs.ThemeMode
import com.vibely.wc26.domain.prefs.UserPreferences
import com.vibely.wc26.domain.prefs.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val ownershipRepository: OwnershipRepository,
) : ViewModel() {

    val state: StateFlow<UserPreferences> = preferences.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), UserPreferences())

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { preferences.setTheme(mode) }
    }

    fun setDefaultSort(sort: PlayerSort) {
        viewModelScope.launch { preferences.setPlayerSort(sort) }
    }

    fun setRapidScan(enabled: Boolean) {
        viewModelScope.launch { preferences.setRapidScan(enabled) }
    }

    /** Wipes ownership. Catalog is untouched — only the user's progress is lost. */
    fun resetProgress() {
        viewModelScope.launch { ownershipRepository.clearAll() }
    }

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}
