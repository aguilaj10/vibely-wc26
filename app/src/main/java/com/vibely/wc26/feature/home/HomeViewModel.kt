package com.vibely.wc26.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.domain.model.CollectionStats
import com.vibely.wc26.domain.usecase.GetOverallStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    getOverallStats: GetOverallStatsUseCase,
) : ViewModel() {

    val state: StateFlow<CollectionStats?> = getOverallStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
