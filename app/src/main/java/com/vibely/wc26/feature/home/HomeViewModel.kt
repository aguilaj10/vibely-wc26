package com.vibely.wc26.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.domain.model.CollectionStats
import com.vibely.wc26.domain.model.Sticker
import com.vibely.wc26.domain.usecase.GetOverallStatsUseCase
import com.vibely.wc26.domain.usecase.ObserveLastAddedStickerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    getOverallStats: GetOverallStatsUseCase,
    observeLastAdded: ObserveLastAddedStickerUseCase,
) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(
        getOverallStats(),
        observeLastAdded(),
    ) { stats, lastAdded ->
        HomeUiState(stats = stats, lastAdded = lastAdded)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS),
        HomeUiState.Empty,
    )

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

data class HomeUiState(
    val stats: CollectionStats?,
    val lastAdded: Sticker?,
) {
    companion object {
        val Empty = HomeUiState(stats = null, lastAdded = null)
    }
}
