package com.vibely.wc26.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.domain.usecase.ObserveSearchResultsUseCase
import com.vibely.wc26.domain.usecase.SearchHit
import com.vibely.wc26.domain.usecase.SearchResultsData
import com.vibely.wc26.domain.usecase.UpdateStickerQuantityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    observeSearchResults: ObserveSearchResultsUseCase,
    private val updateQuantity: UpdateStickerQuantityUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedStickerId = MutableStateFlow<String?>(null)

    fun setQuery(value: String) {
        query.value = value
    }

    fun openSheet(stickerId: String) {
        selectedStickerId.value = stickerId
    }

    fun closeSheet() {
        selectedStickerId.value = null
    }

    fun increment(stickerId: String) {
        viewModelScope.launch { updateQuantity.adjust(stickerId, +1) }
    }

    fun decrement(stickerId: String) {
        viewModelScope.launch { updateQuantity.adjust(stickerId, -1) }
    }

    fun setQuantity(stickerId: String, quantity: Int) {
        viewModelScope.launch { updateQuantity.set(stickerId, quantity) }
    }

    val state: StateFlow<SearchUiState> = combine(
        query,
        observeSearchResults(query),
        selectedStickerId,
    ) { current, results, selectedId ->
        SearchUiState(
            query = current,
            results = results,
            selected = selectedId?.let { id -> findHit(results, id) },
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS),
        SearchUiState.Empty,
    )

    private fun findHit(results: SearchResultsData, stickerId: String): SearchHit? {
        val hits = (results as? SearchResultsData.Hits) ?: return null
        return hits.sections.asSequence()
            .flatMap { it.rows.asSequence() }
            .firstOrNull { it.sticker.id == stickerId }
    }

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

data class SearchUiState(
    val query: String,
    val results: SearchResultsData,
    val selected: SearchHit? = null,
) {
    companion object {
        val Empty = SearchUiState(
            query = "",
            results = SearchResultsData.Idle,
            selected = null,
        )
    }
}
