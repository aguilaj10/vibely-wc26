package com.vibely.wc26.feature.browse.specials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.domain.usecase.ObserveSpecialsUseCase
import com.vibely.wc26.domain.usecase.SpecialStickerRow
import com.vibely.wc26.domain.usecase.SpecialsData
import com.vibely.wc26.domain.usecase.SpecialsSection
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
class SpecialsViewModel @Inject constructor(
    observeSpecials: ObserveSpecialsUseCase,
    private val updateQuantity: UpdateStickerQuantityUseCase,
) : ViewModel() {

    private val selectedStickerId = MutableStateFlow<String?>(null)

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

    val state: StateFlow<SpecialsUiState> = combine(
        observeSpecials(),
        selectedStickerId,
    ) { data, selectedId ->
        SpecialsUiState(
            sections = data.sections,
            selected = selectedId?.let { id -> findRow(data, id) },
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS),
        SpecialsUiState.Empty,
    )

    private fun findRow(data: SpecialsData, stickerId: String): SpecialStickerRow? =
        data.sections.asSequence()
            .flatMap { it.rows.asSequence() }
            .firstOrNull { it.sticker.id == stickerId }

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

data class SpecialsUiState(
    val sections: List<SpecialsSection>,
    val selected: SpecialStickerRow? = null,
) {
    companion object {
        val Empty = SpecialsUiState(sections = emptyList(), selected = null)
    }
}
