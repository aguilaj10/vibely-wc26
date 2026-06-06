package com.vibely.wc26

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.prefs.ThemeMode
import com.vibely.wc26.domain.prefs.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Root-level state owned by [MainActivity]. Two responsibilities:
 *   1. Surface the user's theme override so [com.vibely.wc26.core.ui.theme.PaniniWC26Theme]
 *      can resolve dark/light before any screen renders.
 *   2. Try to load the catalog once at startup and expose [catalogState] so the app can
 *      show a friendly error gate if the asset is corrupt or missing.
 *
 * Kept deliberately small — everything else lives in feature-scoped ViewModels.
 */
@HiltViewModel
class AppShellViewModel @Inject constructor(
    preferences: UserPreferencesRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = preferences.preferences
        .map { it.theme }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.System)

    private val _catalogState = MutableStateFlow<CatalogState>(CatalogState.Loading)
    val catalogState: StateFlow<CatalogState> = _catalogState.asStateFlow()

    init {
        viewModelScope.launch {
            _catalogState.value = runCatching { catalogRepository.load() }
                .fold(
                    onSuccess = { CatalogState.Ready },
                    onFailure = { CatalogState.Error(it.message.orEmpty()) },
                )
        }
    }

    /** Retry catalog load. Used by the error screen. */
    fun retryCatalogLoad() {
        _catalogState.value = CatalogState.Loading
        viewModelScope.launch {
            _catalogState.value = runCatching { catalogRepository.load() }
                .fold(
                    onSuccess = { CatalogState.Ready },
                    onFailure = { CatalogState.Error(it.message.orEmpty()) },
                )
        }
    }
}

sealed interface CatalogState {
    data object Loading : CatalogState
    data object Ready : CatalogState
    data class Error(val message: String) : CatalogState
}
