package com.vibely.wc26.feature.browse.specials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.model.Sticker
import com.vibely.wc26.domain.model.StickerType
import com.vibely.wc26.domain.ownership.OwnershipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SpecialsViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val ownershipRepository: OwnershipRepository,
) : ViewModel() {

    val state: StateFlow<SpecialsUiState> = combine(
        flow { emit(catalogRepository.load()) },
        ownershipRepository.observeAll(),
    ) { catalog, ownership ->
        val specials = catalog.stickers.filter { it.type == StickerType.SPECIAL || it.section != null }
        val bySection = specials.groupBy { it.section ?: "—" }
            .toSortedMap()
            .map { (section, stickers) ->
                SpecialsSection(
                    name = section,
                    rows = stickers.map { SpecialRow(it, ownership[it.id] ?: 0) },
                )
            }
        SpecialsUiState(sections = bySection)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SpecialsUiState.Empty,
    )
}

data class SpecialRow(
    val sticker: Sticker,
    val quantity: Int,
)

data class SpecialsSection(
    val name: String,
    val rows: List<SpecialRow>,
)

data class SpecialsUiState(
    val sections: List<SpecialsSection>,
) {
    companion object {
        val Empty = SpecialsUiState(sections = emptyList())
    }
}
