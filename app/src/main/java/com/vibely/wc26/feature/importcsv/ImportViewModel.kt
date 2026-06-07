package com.vibely.wc26.feature.importcsv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.domain.usecase.ImportOwnershipUseCase
import com.vibely.wc26.domain.usecase.PreviewCsvImportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportViewModel
    @Inject
    constructor(
        private val previewCsv: PreviewCsvImportUseCase,
        private val importOwnership: ImportOwnershipUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(ImportUiState())
        val state: StateFlow<ImportUiState> = _state.asStateFlow()

        fun setCsvInput(csv: String) {
            _state.update {
                it.copy(
                    csvInput = csv,
                    parsedCount = previewCsv(csv),
                )
            }
        }

        fun import() {
            val csv = _state.value.csvInput
            if (csv.isBlank()) return

            viewModelScope.launch {
                _state.update { it.copy(isImporting = true, result = null) }
                val result =
                    runCatching { importOwnership(csv) }
                        .getOrElse { e ->
                            ImportOwnershipUseCase.ImportResult(
                                importedCount = 0,
                                skippedIds = emptyList(),
                                unknownIds = emptyList(),
                                error = e.message ?: "Unknown error",
                            )
                        }
                _state.update {
                    it.copy(
                        isImporting = false,
                        result = result,
                        csvInput = "",
                        parsedCount = 0,
                    )
                }
            }
        }
    }

data class ImportUiState(
    val csvInput: String = "",
    val parsedCount: Int = 0,
    val isImporting: Boolean = false,
    val result: ImportOwnershipUseCase.ImportResult? = null,
)
