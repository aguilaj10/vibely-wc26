package com.vibely.wc26.feature.stats.duplicated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.domain.usecase.DuplicatedData
import com.vibely.wc26.domain.usecase.DuplicatedSort
import com.vibely.wc26.domain.usecase.ObserveDuplicatedStickersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DuplicatedViewModel @Inject constructor(
    observeDuplicated: ObserveDuplicatedStickersUseCase,
) : ViewModel() {

    private val sort = MutableStateFlow(DuplicatedSort.MostDuplicated)

    fun setSort(value: DuplicatedSort) {
        sort.value = value
    }

    val state: StateFlow<DuplicatedData> = observeDuplicated(sort)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), DuplicatedData.Empty)

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}
