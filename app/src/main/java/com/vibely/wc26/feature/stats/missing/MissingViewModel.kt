package com.vibely.wc26.feature.stats.missing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.domain.usecase.MissingData
import com.vibely.wc26.domain.usecase.ObserveMissingStickersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MissingViewModel @Inject constructor(
    observeMissing: ObserveMissingStickersUseCase,
) : ViewModel() {

    val state: StateFlow<MissingData> = observeMissing()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), MissingData.Empty)

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}
