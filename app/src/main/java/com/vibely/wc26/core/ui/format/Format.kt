package com.vibely.wc26.core.ui.format

import com.vibely.wc26.domain.model.ProgressSummary
import java.text.NumberFormat

/**
 * Locale-aware percent label.
 * Respects each region's convention (Mexico/US: period, Spain/Argentina: comma).
 */
val ProgressSummary.percentLabel: String
    get() = NumberFormat.getPercentInstance().apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }.format(progress)

/** Compact "owned / total" label, locale-neutral digits. */
val ProgressSummary.countLabel: String
    get() = "$owned / $total"
