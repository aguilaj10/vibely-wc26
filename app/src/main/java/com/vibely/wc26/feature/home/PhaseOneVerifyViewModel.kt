package com.vibely.wc26.feature.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.ownership.OwnershipRepository
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
 * Phase 1 verification VM — confirms catalog loads from assets and ownership Flow is wired.
 * Will be replaced by HomeViewModel in Phase 3.
 */
@HiltViewModel
internal class PhaseOneVerifyViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    ownershipRepository: OwnershipRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _catalogState = MutableStateFlow<CatalogState>(CatalogState.Loading)
    val catalogState: StateFlow<CatalogState> = _catalogState.asStateFlow()

    val ownedCount: StateFlow<Int> = ownershipRepository
        .observeAll()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        viewModelScope.launch {
            _catalogState.value = runCatching { catalogRepository.load() }
                .fold(
                    onSuccess = { CatalogState.Loaded(
                        version = it.version,
                        totalStickers = it.totalStickers,
                        totalTeams = it.totalTeams,
                        totalSpecials = it.totalSpecials,
                    ) },
                    onFailure = { CatalogState.Error(it.message ?: "Unknown error") },
                )
        }
    }
}

internal sealed interface CatalogState {
    data object Loading : CatalogState
    data class Loaded(
        val version: String,
        val totalStickers: Int,
        val totalTeams: Int,
        val totalSpecials: Int,
    ) : CatalogState
    data class Error(val message: String) : CatalogState
}
