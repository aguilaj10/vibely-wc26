package com.vibely.wc26.feature.scan

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.domain.model.StickerMatch
import com.vibely.wc26.domain.ownership.OwnershipRepository
import com.vibely.wc26.domain.prefs.UserPreferencesRepository
import com.vibely.wc26.domain.usecase.MatchScannedTextUseCase
import com.vibely.wc26.domain.usecase.UpdateStickerQuantityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * State machine for the Scan feature.
 *
 *  Idle ──capture──▶ Reading ──OCR──▶ Matched(best, alts) ──save──▶ Idle
 *                                 └──▶ NoMatch ──tryAgain──▶ Idle
 *
 * CameraX lives in the screen layer (Android-lifecycle bound). The VM owns
 * everything else: OCR, fuzzy matching, ownership writes, rapid-mode toggle.
 */
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val matchScannedText: MatchScannedTextUseCase,
    private val updateQuantity: UpdateStickerQuantityUseCase,
    private val ownershipRepository: OwnershipRepository,
    private val preferences: UserPreferencesRepository,
    private val textRecognitionSource: TextRecognitionSource,
) : ViewModel() {

    private val _state = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    val rapidModeEnabled: StateFlow<Boolean> = preferences.preferences
        .map { it.rapidScan }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), false)

    fun setRapidMode(enabled: Boolean) {
        viewModelScope.launch { preferences.setRapidScan(enabled) }
    }

    /**
     * Process a captured frame end-to-end: run OCR, match, update state.
     * The screen passes the raw bitmap + CameraX rotation hint; this method
     * handles the rest. Marks Reading state before OCR so the UI can show a
     * spinner; transitions to Matched / NoMatch on completion.
     */
    fun onFrameCaptured(bitmap: Bitmap, rotationDegrees: Int) {
        _state.value = ScanUiState.Reading
        viewModelScope.launch {
            val recognized = runCatching {
                textRecognitionSource.recognize(bitmap, rotationDegrees)
            }.getOrElse { "" }

            val matches = matchScannedText(recognized, limit = ALTERNATIVES_LIMIT)
            if (matches.isEmpty()) {
                _state.value = ScanUiState.NoMatch(rawText = recognized)
                return@launch
            }

            val best = matches.first()
            val rapidMode = preferences.preferences.first().rapidScan
            if (rapidMode && best.score >= RAPID_MODE_THRESHOLD) {
                updateQuantity.adjust(best.sticker.id, +1)
                _state.value = ScanUiState.Idle
                return@launch
            }

            val currentQty = ownershipRepository.getQuantity(best.sticker.id)
            _state.value = ScanUiState.Matched(
                best = best,
                alternatives = matches.drop(1),
                ownedQuantity = currentQty,
                pendingQuantity = currentQty + 1,
            )
        }
    }

    fun onCaptureFailed() {
        _state.value = ScanUiState.NoMatch(rawText = "")
    }

    fun selectAlternative(stickerId: String) {
        val current = _state.value as? ScanUiState.Matched ?: return
        val all = listOf(current.best) + current.alternatives
        val newBest = all.firstOrNull { it.sticker.id == stickerId } ?: return
        viewModelScope.launch {
            val currentQty = ownershipRepository.getQuantity(newBest.sticker.id)
            _state.value = current.copy(
                best = newBest,
                alternatives = all.filter { it.sticker.id != stickerId },
                ownedQuantity = currentQty,
                pendingQuantity = currentQty + 1,
            )
        }
    }

    /** Stepper [+]: bump pending quantity by 1. Does not persist. */
    fun incrementPending() {
        val current = _state.value as? ScanUiState.Matched ?: return
        _state.value = current.copy(pendingQuantity = current.pendingQuantity + 1)
    }

    /** Stepper [−]: drop pending quantity by 1, floored at 0. Does not persist. */
    fun decrementPending() {
        val current = _state.value as? ScanUiState.Matched ?: return
        _state.value = current.copy(
            pendingQuantity = (current.pendingQuantity - 1).coerceAtLeast(0),
        )
    }

    /** Commit the pending quantity and return to Idle so the user can scan the next one. */
    fun commit() {
        val current = _state.value as? ScanUiState.Matched ?: return
        viewModelScope.launch {
            updateQuantity.set(current.best.sticker.id, current.pendingQuantity)
            _state.value = ScanUiState.Idle
        }
    }

    fun dismiss() {
        _state.value = ScanUiState.Idle
    }

    private companion object {
        const val ALTERNATIVES_LIMIT = 3
        /** Auto-commit threshold for rapid mode. Jaro-Winkler score in [0, 1]. */
        const val RAPID_MODE_THRESHOLD = 0.85f
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data object Reading : ScanUiState
    data class Matched(
        val best: StickerMatch,
        val alternatives: List<StickerMatch>,
        /** What the user actually owns right now. Used to label the stepper baseline. */
        val ownedQuantity: Int,
        /** What the user has dialed via the stepper. Committed on tap of the Save CTA. */
        val pendingQuantity: Int,
    ) : ScanUiState
    data class NoMatch(val rawText: String) : ScanUiState
}
