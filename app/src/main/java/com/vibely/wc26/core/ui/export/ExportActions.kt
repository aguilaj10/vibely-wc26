package com.vibely.wc26.core.ui.export

import android.content.ClipData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import com.vibely.wc26.core.util.ShareLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Bundles clipboard + share-sheet actions used by the Stats tabs (Missing,
 * Duplicated). Build once per screen via [rememberExportActions]; the returned
 * [copy] / [share] entry points take the payload as input — encourages callers
 * to compute export text lazily on click rather than on every recomposition.
 *
 * Uses the new (Compose 1.7+) [Clipboard] API, which is suspend-based. The
 * caller's scope is captured at remember time and survives recompositions.
 */
class ExportActions internal constructor(
    private val clipboardLabel: String,
    private val clipboard: Clipboard,
    private val scope: CoroutineScope,
    private val shareLauncher: ShareLauncher,
) {
    fun copy(text: String) {
        if (text.isBlank()) return
        scope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(clipboardLabel, text)))
        }
    }

    fun share(text: String) {
        shareLauncher.share(text)
    }
}

@Composable
fun rememberExportActions(clipboardLabel: String): ExportActions {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val shareLauncher = remember(context) { ShareLauncher(context) }
    return remember(clipboardLabel, clipboard, scope, shareLauncher) {
        ExportActions(
            clipboardLabel = clipboardLabel,
            clipboard = clipboard,
            scope = scope,
            shareLauncher = shareLauncher,
        )
    }
}
