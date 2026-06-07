package com.vibely.wc26.feature.importcsv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.data.ownership.OwnershipCsvParser
import com.vibely.wc26.domain.usecase.ImportOwnershipUseCase
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
        private val importOwnership: ImportOwnershipUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(ImportUiState())
        val state: StateFlow<ImportUiState> = _state.asStateFlow()

        fun setCsvInput(csv: String) {
            val result = OwnershipCsvParser.parse(csv)
            _state.update {
                it.copy(
                    csvInput = csv,
                    parsedCount = result.quantities.size,
                )
            }
        }

        fun import() {
            val csv = _state.value.csvInput
            if (csv.isBlank()) return

            viewModelScope.launch {
                _state.update { it.copy(isImporting = true, resultMessage = null, isError = false) }
                try {
                    val result = importOwnership(csv)
                    val message =
                        buildString {
                            appendLine("Imported ${result.importedCount} stickers")
                            if (result.skippedIds.isNotEmpty()) {
                                appendLine("Skipped ${result.skippedIds.size} (quantity ≤ 0)")
                            }
                            if (result.unknownIds.isNotEmpty()) {
                                appendLine("Unknown IDs: ${result.unknownIds.joinToString(", ")}")
                            }
                        }.trim()
                    _state.update {
                        it.copy(
                            isImporting = false,
                            resultMessage = message,
                            isError = false,
                            csvInput = "",
                            parsedCount = 0,
                        )
                    }
                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            isImporting = false,
                            resultMessage = "Import failed: ${e.message}",
                            isError = true,
                        )
                    }
                }
            }
        }
    }

data class ImportUiState(
    val csvInput: String = "",
    val parsedCount: Int = 0,
    val isImporting: Boolean = false,
    val resultMessage: String? = null,
    val isError: Boolean = false,
)
