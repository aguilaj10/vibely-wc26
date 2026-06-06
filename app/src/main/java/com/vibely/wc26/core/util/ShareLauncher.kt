package com.vibely.wc26.core.util

import android.content.Context
import android.content.Intent

/**
 * Thin wrapper around `ACTION_SEND` for plain text. Used by the Stats tabs to
 * share Missing / Duplicated lists. Kept as a class (not a top-level function)
 * so callers can hold a single instance scoped to the screen — avoids creating
 * a new lambda capture on every recomposition.
 */
class ShareLauncher(private val context: Context) {

    fun share(text: String, chooserTitle: String? = null) {
        if (text.isBlank()) return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            // Required because callers pass an application context in some flows.
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
